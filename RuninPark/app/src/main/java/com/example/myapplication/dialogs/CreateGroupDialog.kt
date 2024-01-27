package com.example.myapplication.dialogs
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.viewModel.UserViewModel

abstract class CreateGroupDialog(context: Context, private val viewModel: UserViewModel, private val frag: Fragment): Dialog(context) {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState ?: Bundle())


        val view = LayoutInflater.from(context).inflate(R.layout.create_group_dialog, null)


        setContentView(view)

        //Permite cancelar o dialog clicando fora da view deste
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        //Ir buscar os elementos do dialog por ID
       val button = view.findViewById<Button>(R.id.create_run_btn)
        val name = view.findViewById<AppCompatEditText>(R.id.group_name_dialog)
        val city = view.findViewById<AppCompatEditText>(R.id.group_city_dialog)

        //Ser chamada a função que tenta criar um grupo,e de seguida cancela o dialog
        button.setOnClickListener{
            viewModel.createGroup(name.text.toString(),city.text.toString(),frag)
            cancel()
        }





    }



}