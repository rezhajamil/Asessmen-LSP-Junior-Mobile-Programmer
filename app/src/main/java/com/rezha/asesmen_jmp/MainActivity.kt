package com.rezha.asesmen_jmp

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var mDatabase: DatabaseReference

    private var dataList=ArrayList<Data>()
    var refData=FirebaseDatabase.getInstance().getReference("Data")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_add.setOnClickListener {
            startActivity(Intent(this@MainActivity,FormActivity::class.java))
        }

        rv_data.layoutManager=LinearLayoutManager(this)
        getData()
    }

    private fun getData() {
        refData.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                dataList.clear()
                for(getSnapshot in snapshot.children){
                    var data=getSnapshot.getValue(Data::class.java)
                    dataList.add(data!!)
                }

                rv_data.adapter=DataAdapter(dataList){
                    var intent=Intent(this@MainActivity,FormActivity::class.java).putExtra("data",it)
                    startActivity(intent)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity,""+error.message, Toast.LENGTH_LONG).show()
            }

        })
    }

}