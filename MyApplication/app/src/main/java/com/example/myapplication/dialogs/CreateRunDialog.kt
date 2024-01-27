package com.example.myapplication.dialogs
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatEditText
import com.example.myapplication.R
import com.example.myapplication.viewModel.UserViewModel


abstract class CreateRunDialog(context: Context, private val viewModel: UserViewModel): Dialog(context) {


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
       val createRunBtn = view.findViewById<Button>(R.id.create_run_btn)
        val hours = view.findViewById<AppCompatEditText>(R.id.run_hours_dlg)
        val minutes = view.findViewById<AppCompatEditText>(R.id.run_mins_dlg)
        val seconds = view.findViewById<AppCompatEditText>(R.id.run_secs_dlg)
        val distance = view.findViewById<AppCompatEditText>(R.id.run_dist_dlg)
        val img = view.findViewById<ImageView>(R.id.run_img_dlg)
        val selImg = view.findViewById<ImageButton>(R.id.btn_sel_foto_run)

        createRunBtn.setOnClickListener{
            viewModel.createRun(viewModel.selectedGroup.value!!.groupId,null,distance.text.toString().toDouble(),hours.text.toString().toInt(),minutes.text.toString().toInt(),seconds.text.toString().toInt())
            cancel()
        }


       }






    }




