package com.rezha.asesmen_jmp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DataAdapter(private var data:List<Data>,
                   private val listener:(Data)-> Unit) : RecyclerView.Adapter<DataAdapter.ViewHolder>() {

    lateinit var contextAdapter: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataAdapter.ViewHolder {
        val layoutInflater= LayoutInflater.from(parent.context)
        contextAdapter=parent.context
        val inflatedView=layoutInflater.inflate(R.layout.row_item,parent,false)
        return ViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: DataAdapter.ViewHolder, position: Int) {
        holder.bindItem(data[position],listener,contextAdapter)
    }

    override fun getItemCount(): Int =data.size

    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        private val tvNama: TextView =view.findViewById(R.id.tv_row_nama)


        fun bindItem(data: Data, listener: (Data) -> Unit, context: Context){
            tvNama.setText(data.nama)

            itemView.setOnClickListener{
                listener(data)
            }

        }

    }
}