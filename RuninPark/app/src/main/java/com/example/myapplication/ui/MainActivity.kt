package com.example.myapplication.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.model.Token
import com.example.myapplication.viewModel.UserViewModel

/*
O facto da aplicação usar apenas uma activity e ir trocando de fragmentos é inspirado por este vídeo:
https://www.youtube.com/watch?v=jl1tDYyf0kc

Bibliotecas:

View Binding ->Substitui o findViewById,e permite aceder a elementos da view
https://developer.android.com/topic/libraries/view-binding?hl=pt-br

Glide -> Permite carregar e inserir imagens remotas(e não só,mas é para este efeito que está a ser usado=,
possui ferramentas de cache e várias outras funcionalidades(como poder dar crop de uma imagem em forma de círculo),
ter uma imagem para cenário de erro,de loading,etc
https://github.com/bumptech/glide

MaskedEditText ->Facilita saneamento de input pelo utilizador,permitindo criar uma máscara para um EditText
 Mais usado para garantir que data de nascimento tem um formato específico(DD/MM/YYYY)
 https://github.com/VicMikhailau/MaskedEditText

 Retrofit -> Para interagir com a API,permitindo converter objetos em algo que é transmitido através de HTTP,e vice versa
 https://square.github.io/retrofit/

 OKHTTP -> Para interagir com a API
 https://square.github.io/okhttp/

 GSON ->Usado para converter texto em formato Json para Objetos(e vice-versa)
 https://github.com/google/gson
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: UserViewModel by viewModels()
    private lateinit var sharedPreferences: SharedPreferences



    override fun onCreate(savedInstanceState: Bundle?) {
        //Cria instância de sharedPreference com nome de "token"
        sharedPreferences=this.getSharedPreferences("token", MODE_PRIVATE)

        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


//Esconde teclado quando se toca no layout activity
        binding.activityMainLayout.setOnClickListener{
            hideKeyboard(it)
        }
//Se utilizador tiver autenticado
        viewModel.loggedIn.observe(this, Observer {
            if(it==true ){
                login()//faz o login

            }else{
                //Tenta autenticar com token,que poderá estar armazenado em memória do telemóvel
                getUserToken()?.let { token -> viewModel.getUserWithToken(token,this@MainActivity) }
                //Apresenta página de login
                replaceFragment(LoginFragment())

            }
        })
        //Bottom navigation view
        binding.bottomNavView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.item_groups -> replaceFragment(GroupsFragment())
                R.id.item_login -> replaceFragment(LoginFragment())
                R.id.item_dashboard -> replaceFragment(DashBoardFragment())
                R.id.item_my_groups -> replaceFragment(GroupsFragment())
                R.id.item_logout -> logout()
                R.id.item_about -> replaceFragment(AboutUsFragment())
            }
            true
        }
    }

    //Devolve token de um utilizador que está guardado na sharedPreference
    private fun getUserToken() : String?{
                return sharedPreferences.getString("token",null)
    }
//Guarda token de um utilizador na sharedPreference
    private fun saveUserToken(token: Token?){
        sharedPreferences.edit().putString("token",token?.token).apply()

    }

    //Limpa grande parte das variáveis que pertencem a cada utilizador
    private fun logout(){
        val builder = AlertDialog.Builder(this@MainActivity)
        builder.setMessage("Are you sure you want to log out?")
            .setCancelable(false)
            .setPositiveButton("Yes") {_, _ ->
                saveUserToken(null)
                viewModel.selectedGroup.value = null
                viewModel.setLoggedIn(false)
                viewModel.setUser(null)
                binding.bottomNavView.menu.clear()
                binding.bottomNavView.inflateMenu(R.menu.bottom_nav)
                supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                Toast.makeText(this,"Logged Out Successful!",Toast.LENGTH_LONG).show()
                viewModel.userGroups.value = null
                viewModel.allGroups.value = null
                //Dá reset à activity
                //https://stackoverflow.com/questions/3053761/reload-activity-in-android
                val int = intent
                finish()
                startActivity(int)

            }
            .setNegativeButton("No") { dialog, _ ->
                
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }






    private fun login(){
        //replaceFragment(GroupsFragment())
        //Se checkbox de "lembrar utilizador" tiver "checked",guarda token do user na sharedPreference
if(findViewById<CheckBox>(R.id.remember).isChecked){ saveUserToken(viewModel.user.value?.token) }
        //limpa backstack de fragmentos
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        //limpa bottomNavBar existente e substitui com uma nova
        binding.bottomNavView.menu.clear()
        binding.bottomNavView.inflateMenu(R.menu.loggedin_bottom_nav)

       viewModel.allGroups.value=null
        //Redireciona para dashsboard
        replaceFragment(DashBoardFragment())


    }
    companion object{
        //Esconde teclado
        fun Context.hideKeyboard(view: View) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

//Substitui um fragmento por outro
    private fun replaceFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout,fragment)

        fragmentTransaction.addToBackStack(null).commit()

    }
    override fun onBackPressed() {
        if(supportFragmentManager.backStackEntryCount==1){
            //Se for último fragmento no stack(dashboard ou login),minimiza a aplicação quando clicado no botão "back"
            this.moveTaskToBack(true);
        }else{
            //remove fragmento do stack
            supportFragmentManager.popBackStack()
        }
    }

}
