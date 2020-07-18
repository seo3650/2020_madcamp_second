package com.example.madcamp_second.Contact

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tab.R
import com.example.tab.contact.ContactService
import com.google.gson.JsonObject
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val MULTIPLE_PERMISSION_REQUEST = 0
private const val REQUEST_EDIT = 3
private const val REQUEST_INSERT = 3

class ContactFragment : Fragment() {

    private lateinit var contactViewModel: ContactViewModel
    private lateinit var adapter: ContactAdapter
    private val url = "http://192.249.19.244:2280/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_contact, container, false)

        /* Remove existing observer */

        adapter =
            ContactAdapter({ contact, _, _ ->
                edit(contact)
            }, { contact ->
                deleteDialog(contact)
            })

        val lm = LinearLayoutManager(context)
        val recyclerView = root.findViewById<RecyclerView>(R.id.main_recycleview)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = lm
        recyclerView.setHasFixedSize(true)

        /* Get contacts */
        if (context?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.READ_CONTACTS) }
            == PackageManager.PERMISSION_GRANTED) {
            contactViewModel = ViewModelProviders.of(this).get(ContactViewModel::class.java)
            val contacts = contactViewModel.getAll(context!!)
            contacts.observe(activity!!, Observer<List<Contact>> { contacts ->
                adapter.setContacts(contacts!!)
                saveToDatabase(contacts)
            })
        }


        val addButton = root.findViewById<ImageView>(R.id.main_button)

        addButton.setOnClickListener {
            insert()
        }

        return root
    }

    private fun checkPermissions() {
        /* Set permission */
        var rejectedPermission = ArrayList<String>()
        val requiredPermission: ArrayList<String> = arrayListOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        )

        for (permission in requiredPermission) {
            if (context?.let { ContextCompat.checkSelfPermission(it,permission) }
                != PackageManager.PERMISSION_GRANTED) {
                rejectedPermission.add(permission)
            }
        }

        if(rejectedPermission.isNotEmpty()) {
            val array = arrayOfNulls<String>(rejectedPermission.size)
            requestPermissions(
                rejectedPermission.toArray(array),
                MULTIPLE_PERMISSION_REQUEST
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            MULTIPLE_PERMISSION_REQUEST -> {
                if (grantResults.isEmpty()) {
                    return
                }
                for ((i, _) in permissions.withIndex()) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        checkPermissions()
                    }
                    if (i == 0) {
                        contactViewModel = ViewModelProviders.of(this).get(ContactViewModel::class.java)
                        context?.let {
                            contactViewModel.getAll(it).observe(this, Observer<List<Contact>> { contacts ->
                                adapter.setContacts(contacts!!)
                                saveToDatabase(contacts)
                            })
                        }
                    }
                }
            }
        }
    }

    private fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        observe(lifecycleOwner, object: Observer<T> {
            override fun onChanged(t: T?) {
                observer.onChanged(t)
                removeObserver(this)
            }
        })
    }

    private fun deleteDialog(contact: Contact) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage("Delete selected contact?")
            .setNegativeButton("NO") { _, _ -> }
            .setPositiveButton("YES") { _, _ ->
                context?.let { contactViewModel.delete(it, contact) }
            }
        builder.show()
    }

    private fun insert() {
        val intent = Intent(ContactsContract.Intents.Insert.ACTION).apply {
            type = ContactsContract.RawContacts.CONTENT_TYPE
        }
        startActivityForResult(intent, REQUEST_INSERT)
    }

    private fun edit(contact: Contact) {
        val id: String = contact.id.toString()
        val section = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?"
        val sectionArgs = arrayOf(id)
        val c: Cursor = context?.contentResolver?.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null, section, sectionArgs,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " asc")
            ?: return

        if (c.moveToFirst()) {
            try {
                do {
                    val lookupKey = c.getString(c.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY))
                    val uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey)

                    val editIntent: Intent = Intent(Intent.ACTION_EDIT).apply {
                        setDataAndType(uri, ContactsContract.Contacts.CONTENT_ITEM_TYPE)
                    }
                    editIntent.putExtra("finishActivityOnSaveCompleted", true)
                    editIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    startActivityForResult(editIntent, REQUEST_EDIT)

                } while (c.moveToNext())
            } catch (e: Exception) {
                e.stackTrace
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_EDIT) {
            contactViewModel.getAll(context!!)
        } else if (requestCode == REQUEST_INSERT) {
            contactViewModel.getAll(context!!)
        }
    }

    private fun saveToDatabase(contacts: List<Contact>) {
        /* Init retrofit */
        val retrofit = Retrofit.Builder()
                .baseUrl(this.url)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        val service = retrofit.create(ContactService::class.java)

        /* Prepare request body */
        for (contact in contacts) {
            val info = JsonObject()
            info.addProperty("id", contact.id)
            info.addProperty("name", contact.name)
            info.addProperty("number", contact.number)

            val body = HashMap<String, JsonObject>()
            body["user"] = info

            service.addContact(body)?.enqueue(object: Callback<String> {
                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.d("ContactService", "Failed API call with call: " + call
                            + ", exception:  " + t)
                }

                override fun onResponse(call: Call<String>, response: Response<String>) {
                    Log.d("ContactService", "res:" + response.body().toString())
                }
            })
        }

    }

    companion object {
        @JvmStatic
        fun newInstance(): ContactFragment {
            return ContactFragment().apply {
                arguments = Bundle().apply {
                }
            }
        }
    }
}