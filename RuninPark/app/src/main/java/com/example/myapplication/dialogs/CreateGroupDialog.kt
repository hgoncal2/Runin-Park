package com.example.myapplication.dialogs
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.ui.adapter.AddGroupAdapter
import com.example.myapplication.viewModel.UserViewModel


abstract class CreateGroupDialog(context: Context, private val viewModel: UserViewModel, private val frag: Fragment): Dialog(context) {
     var adapter: AddGroupAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState ?: Bundle())


        val view = LayoutInflater.from(context).inflate(R.layout.create_group_dialog, null)


        setContentView(view)


        setCanceledOnTouchOutside(true)

setOnDismissListener{
    cancel()
}
        setOnCancelListener {
            cancel()
        }
        setCancelable(true)
        // Set up the RecyclerView in the dialog
       val button = view.findViewById<Button>(R.id.create_run_btn)
        val name = view.findViewById<AppCompatEditText>(R.id.group_name_dialog)
        val city = view.findViewById<AppCompatEditText>(R.id.group_city_dialog)

        button.setOnClickListener{
            viewModel.createGroup(name.text.toString(),city.text.toString(),frag)
            cancel()
        }





    }



}