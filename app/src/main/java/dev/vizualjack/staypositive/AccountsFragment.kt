package dev.vizualjack.staypositive

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.core.view.setMargins
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dev.vizualjack.staypositive.databinding.FragmentAccountsBinding
import kotlinx.coroutines.*
import java.util.ArrayList


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class AccountsFragment : Fragment() {

    private var _binding: FragmentAccountsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var mainActivity: MainActivity? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAccountsBinding.inflate(inflater, container, false)
        mainActivity = activity as MainActivity
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadAccounts()
        binding.fab.setOnClickListener { view ->
            mainActivity!!.selectedAccount = null
            namePopup("")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun namePopup(value: String?) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Enter account name")
        val input = EditText(context)
        if (value != null) input.setText(value)
        builder.setView(input)
        builder.setPositiveButton("OK",
            DialogInterface.OnClickListener { dialog, which ->
                if(input.text.isEmpty()) return@OnClickListener
                val accountName = input.text.toString()
                if (mainActivity!!.selectedAccount == null)
                    mainActivity!!.accounts.add(Account(accountName, 0f, ArrayList<Payment>()))
                else
                    mainActivity!!.selectedAccount!!.name = accountName
                mainActivity!!.save()
                loadAccounts()
            })
        builder.setNegativeButton("Cancel",
            DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
        builder.show()
    }

    private fun loadAccounts() {
        val accountsWrapper = requireView().findViewById<LinearLayout>(R.id.entriesWrapper)
        accountsWrapper.removeAllViews()
        for (account in mainActivity!!.accounts) {
            val newEntry = LayoutInflater.from(context).inflate(R.layout.fragment_accounts_entry, null)
            val nameView = newEntry.findViewById<TextView>(R.id.overlay_entry_nameView)
            val cashView = newEntry.findViewById<TextView>(R.id.overlay_entry_cashView)
            nameView.text = account.name
            var cashViewText = Util.toNiceString(account.cash!!, true)
            var preSign = "+"
            var colorId = R.color.green
            if (account.cash!! < 0f) {
                preSign = "-"
                colorId = R.color.red
            }
            cashView.text = "$preSign $cashViewText €"
            cashView.setTextColor(resources.getColor(colorId, null))
            newEntry.setOnClickListener {
                mainActivity!!.selectedAccount = account
                findNavController().navigate(R.id.action_AccountsFragment_to_OverlayFragment)
            }
            newEntry.setOnLongClickListener {
                mainActivity!!.selectedAccount = account
                namePopup(account.name)
                true
            }
            accountsWrapper.addView(newEntry)
            newEntry.layoutParams.height = 100 * requireContext().resources.displayMetrics.density.toInt()
            val marginLayout = newEntry.layoutParams as ViewGroup.MarginLayoutParams
            marginLayout.setMargins(30)
        }
    }
}