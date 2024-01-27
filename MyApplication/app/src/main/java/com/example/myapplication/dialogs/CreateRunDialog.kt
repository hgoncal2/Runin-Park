package com.example.myapplication.dialogs
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.viewModel.UserViewModel


abstract class CreateRunDialog(context: Context, private val viewModel: UserViewModel, private val frag: Fragment): Dialog(context) {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState ?: Bundle())


        val view = LayoutInflater.from(context).inflate(R.layout.add_run_dialog, null)


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
        val hours = view.findViewById<AppCompatEditText>(R.id.run_hours_dlg)
        val minutes = view.findViewById<AppCompatEditText>(R.id.run_mins_dlg)
        val seconds = view.findViewById<AppCompatEditText>(R.id.run_secs_dlg)
        val rating = view.findViewById<RatingBar>(R.id.rate_run_dlg)
        val distance = view.findViewById<AppCompatEditText>(R.id.run_dist_dlg)
        val img = view.findViewById<ImageView>(R.id.run_image_dlg)

        button.setOnClickListener{
            //viewModel.createGroup(name.text.toString(),city.text.toString(),frag)
            cancel()
        }





    }



}