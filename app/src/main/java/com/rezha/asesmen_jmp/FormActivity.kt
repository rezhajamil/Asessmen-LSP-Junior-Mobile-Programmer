package com.rezha.asesmen_jmp

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_form.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class FormActivity : AppCompatActivity() {
    private val refData=FirebaseDatabase.getInstance().getReference("Data")
    private lateinit var ImageUri:Uri
    var filename=""
    lateinit var address:String
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val data=intent.getParcelableExtra<Data>("data")
        val inflater: MenuInflater = menuInflater
        if (data!=null){
            inflater.inflate(R.menu.menu, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        val data=intent.getParcelableExtra<Data>("data")
        return when (item.itemId) {
            R.id.menu_hapus -> {
                hapusData(data!!)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form)

        fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(this)

        val data=intent.getParcelableExtra<Data>("data")
        if(data!=null){
            editData(data)
        }

        btn_lokasi.setOnClickListener {
            fetchLocation()
        }

        iv_upload.setOnClickListener {
            selectImage()
        }

        btn_submit.setOnClickListener {
            var radioButton=findViewById<RadioButton>(rg_gender.checkedRadioButtonId)
            var sNama=et_nama.text.toString()
            var sHp=et_hp.text.toString()
            var sAlamat=et_alamat.text.toString()
            var sGender=radioButton.text.toString()

            var sLokasi=et_lokasi.text.toString()
            var sID:String
            var sFoto:String
            if(data!=null){
                sID=data.id.toString()
                sFoto=data.foto.toString()
            }
            else{
                sID=refData.push().key.toString()
                sFoto=filename
            }

            if (filename!="")uploadImage()
            saveData(sNama,sHp,sAlamat,sGender,sFoto,sLokasi,sID)


        }
    }

    private fun fetchLocation() {
        val task=fusedLocationProviderClient.lastLocation
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Checking Location...")
        progressDialog.setCancelable(false)
        if (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)
            !=PackageManager.PERMISSION_GRANTED && ActivityCompat
            .checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED
        ){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),101)
            return
        }

        progressDialog.show()
        task.addOnSuccessListener {
            if (it!=null){
                var latitude=it.latitude
                var longtitude = it.longitude
                var geocoder=Geocoder(this,Locale.getDefault())
                var addresses=geocoder.getFromLocation(latitude,longtitude,1)
                address = addresses.get(0).getAddressLine(0).toString()

                et_lokasi.setText(address)
                if (progressDialog.isShowing) progressDialog.dismiss()
                Log.v("address",address)
            }
        }
        task.addOnFailureListener {
            if (progressDialog.isShowing) progressDialog.dismiss()
            Toast.makeText(this,"Gagal Mendapatkan Lokasi",Toast.LENGTH_LONG).show()
        }
    }

    private fun uploadImage() {
//
        val storageReference = FirebaseStorage.getInstance().getReference("images/$filename")

        storageReference.putFile(ImageUri)
            .addOnSuccessListener {
//
                        Toast.makeText(this,"Berhasil",Toast.LENGTH_LONG).show()
        }.addOnFailureListener{
//            if (progressDialog.isShowing) progressDialog.dismiss()
            Toast.makeText(this,"Failed",Toast.LENGTH_LONG).show()
        }
    }

    private fun selectImage() {
        val intent = Intent()
        intent.type="image/*"
        intent.action=Intent.ACTION_GET_CONTENT
        startActivityForResult(intent,100)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode==100 &&resultCode== RESULT_OK){
            ImageUri=data?.data!!
            iv_upload.setImageURI(ImageUri)
            val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault())
            val now = Date()
            filename = formatter.format(now)

            Glide.with(this)
                .load(ImageUri)
                .apply(RequestOptions.circleCropTransform())
                .into(iv_upload)
        }
    }

    private fun editData(data: Data) {
        setProfile(data.foto)
        et_nama.setText(data.nama)
        et_hp.setText(data.hp)
        et_alamat.setText(data.alamat)
        et_lokasi.setText(data.lokasi)
        if (data.gender=="Pria") rb_pria.isChecked=true
        else rb_wanita.isChecked=true
    }

    private fun setProfile(foto: String?) {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Mengambil Gambar...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        val storageRef=FirebaseStorage.getInstance().reference.child("images/$foto")

        val localFile= File.createTempFile("tempImage","jpeg")
        storageRef.getFile(localFile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            iv_upload.setImageBitmap(bitmap)
            if (progressDialog.isShowing) progressDialog.dismiss()
        }.addOnFailureListener{
            Toast.makeText(this,"Failed to get Image",Toast.LENGTH_LONG).show()
            if (progressDialog.isShowing) progressDialog.dismiss()
        }
    }

    private fun saveData(
        sNama: String,
        sHp: String,
        sAlamat: String,
        sGender: String,
        sFoto: String,
        sLokasi:String,
        sID: String,
    ) {
        var pendaftar = Data()

        pendaftar.nama=sNama
        pendaftar.hp=sHp
        pendaftar.alamat=sAlamat
        pendaftar.gender=sGender
        pendaftar.id=sID
        pendaftar.foto=sFoto
        pendaftar.lokasi=sLokasi

        refData.child(sID).setValue(pendaftar)
        startActivity(Intent(this@FormActivity,MainActivity::class.java))
        Toast.makeText(this,"Data berhasil disimpan",Toast.LENGTH_LONG).show()
    }


    private fun hapusData(data: Data) {
        var alert= AlertDialog.Builder(this)
        alert.setTitle("Hapus Data")
        alert.setPositiveButton("Hapus", DialogInterface.OnClickListener{
                dialog, which ->
            refData.child(data.id.toString()).removeValue()
            onBackPressed()
            finish()
            Toast.makeText(this,"Data telah dihapus",Toast.LENGTH_SHORT).show()
        })
        alert.setNegativeButton("Batal", DialogInterface.OnClickListener{
                dialog, which ->
            dialog.cancel()
            dialog.dismiss()
        })
        alert.create()
        alert.show()
    }
}