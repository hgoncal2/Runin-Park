package com.ipt.runinpark.dialogs
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import com.ipt.runinpark.R
import com.ipt.runinpark.viewModel.UserViewModel


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







        }









    }




