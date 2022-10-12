package dev.vizualjack.staypositive

import android.animation.ObjectAnimator
import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dev.vizualjack.staypositive.databinding.FragmentPaymentBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate


class SpinnerActivity(private val endDate: ConstraintLayout) : Activity(), AdapterView.OnItemSelectedListener {
    override fun onNothingSelected(parent: AdapterView<*>) {}
    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        val paymentType = PaymentType.values()[pos]
        var alpha = 0f
        if (paymentType == PaymentType.MONTHLY)
            alpha = 1f
        endDate.alpha = alpha
    }
}
/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class PaymentFragment : Fragment() {

    private var _binding: FragmentPaymentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var selectedIndex = -1
    private var saving = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = activity as MainActivity
        binding.type.onItemSelectedListener = SpinnerActivity(binding.endDateWrapper)
        binding.type.setOnTouchListener { view, motionEvent ->
            hideKeyboard()
            view.performClick()
        }
        ArrayAdapter(requireContext(), R.layout.spinner_item, PaymentType.values())
            .also { adapter ->
                adapter.setDropDownViewResource(R.layout.spinner_item)
                binding.type.adapter = adapter
            }
        binding.name.setOnFocusChangeListener { view, focus ->
            var value = 0f
            val filled = activity.findViewById<EditText>(R.id.name).text.toString().isNotEmpty()
            if (focus || filled) value = -30f
            changeYpos(R.id.nameText, value)
        }
        binding.value.setOnFocusChangeListener { view, focus ->
            var value = 0f
            if (!focus) binding.value.setText(getCash().toString())
            val valueString = activity.findViewById<EditText>(R.id.value).text.toString()
            val filled = valueString.isNotEmpty()
            if (focus || filled) value = -30f
            changeYpos(R.id.valueText, value)
            if (focus && filled && valueString.toFloat() == 0f) binding.value.setText("")
        }
        binding.date.setOnClickListener {
            hideKeyboard()
            val datePicker = DatePickerDialog(requireContext())
            datePicker.setOnDateSetListener { datePicker, year, month, day ->
                var localDate = LocalDate.of(datePicker.year,datePicker.month+1,datePicker.dayOfMonth)
                if(localDate < LocalDate.now()) localDate = LocalDate.now()
                binding.date.text = Util.toNiceString(localDate)
                changeYpos(R.id.dateText, -30f)
            }
            datePicker.show()
        }
        binding.endDate.setOnClickListener {
            val paymentType = PaymentType.values()[binding.type.selectedItemPosition]
            if (paymentType != PaymentType.MONTHLY) return@setOnClickListener
            hideKeyboard()
            val datePicker = DatePickerDialog(requireContext())
            datePicker.setOnDateSetListener { datePicker, year, month, day ->
                var localDate = LocalDate.of(datePicker.year,datePicker.month+1,datePicker.dayOfMonth)
                if(localDate < LocalDate.now()) localDate = LocalDate.now()
                if (binding.date.text != "") {
                    val cutDate = binding.date.text.toString().split('.')
                    val date = LocalDate.of(cutDate[2].toInt(), cutDate[1].toInt(), cutDate[0].toInt())
                    if (date > localDate) localDate = date
                }
                binding.endDate.text = Util.toNiceString(localDate).substring(3)
                changeYpos(R.id.endDateText, -30f)
            }
            datePicker.show()
        }
        binding.saveBtn.setOnClickListener {
            if (saving || binding.name.text.isEmpty() ||
                binding.value.text.isEmpty() ||
                binding.date.text.isEmpty()) return@setOnClickListener
            var testPayment = Payment(binding.name.text.toString(),getCash(),
                null,null, PaymentType.values()[binding.type.selectedItemPosition])
            val cutDate = binding.date.text.toString().split('.')
            val date = LocalDate.of(cutDate[2].toInt(), cutDate[1].toInt(), cutDate[0].toInt())
            testPayment.nextTime = date
            if (binding.endDate.text != "") {
                val cuttedEndDate = binding.endDate.text.toString().split('.')
                val endDate = LocalDate.of(cuttedEndDate[1].toInt(), cuttedEndDate[0].toInt(), cutDate[0].toInt())
                testPayment.lastTime = endDate
            }
            saving = true
            binding.saveBtn.text = "Calculating..."
            GlobalScope.launch(Dispatchers.IO) {
                var payments = ArrayList<Payment>()
                payments.addAll(activity.selectedAccount!!.payments!!)
                if (selectedIndex != -1) payments.remove(activity.selectedAccount!!.payments!![selectedIndex])
                var stayingPositive = true
                if (testPayment.value!! < 0f)
                    stayingPositive = PaymentUtil.testPayment(testPayment, payments, activity.selectedAccount!!.cash!!)
                saving = false
                withContext(Dispatchers.Main) {
                    if (stayingPositive) {
                        payments.add(testPayment)
                        if (payments.size > 1) payments = PaymentUtil.sortPayments(payments).toList() as ArrayList<Payment>
                        activity.selectedAccount!!.payments = payments
                        activity.save()
                        findNavController().navigateUp()
                    }
                    else {
                        binding.saveBtn.text = "Not possible"
                    }
                }
            }
        }
        if(arguments != null)
            selectedIndex = requireArguments().getInt("index", -1)
        if (selectedIndex == -1) return
        binding.deleteBtn.alpha = 1f
        binding.deleteBtn.setOnClickListener {
            activity.selectedAccount!!.payments!!.removeAt(selectedIndex)
            findNavController().navigateUp()
        }
        val entry = activity.selectedAccount!!.payments!![selectedIndex]
        binding.type.setSelection(entry.type!!.ordinal)
        binding.name.text.insert(0, entry.name)
        changeYpos(R.id.nameText, -30f)
        var value = Util.toNiceString(entry.value!!, false)
        if (entry.value!! < 0f) value = "-$value"
        binding.value.text.insert(0, value)
        changeYpos(R.id.valueText, -30f)
        binding.date.text = Util.toNiceString(entry.nextTime!!)
        changeYpos(R.id.dateText, -30f)
        if (entry.lastTime != null) {
            binding.endDate.text = Util.toNiceString(entry.lastTime!!).substring(3)
            changeYpos(R.id.endDateText, -30f)
        }
    }

    private fun getCash(): Float? {
        var cashText = binding.value.text.toString()
        if (cashText == "") cashText = "0"
        return Util.roundToCashLikeValue(cashText.toFloat())
    }

    fun hideKeyboard() {
        requireView().clearFocus()
        val imm: InputMethodManager? = getSystemService(requireContext(), InputMethodManager::class.java)
        imm!!.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    fun changeYpos(id: Int, newY: Float) {
        ObjectAnimator.ofFloat(requireActivity().findViewById(id), "translationY", newY).apply {
            duration = 500
            start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}