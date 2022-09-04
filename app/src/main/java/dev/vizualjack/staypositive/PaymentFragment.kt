package dev.vizualjack.staypositive

import android.animation.ObjectAnimator
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dev.vizualjack.staypositive.databinding.FragmentPaymentBinding
import java.time.LocalDate


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class PaymentFragment : Fragment() {

    private var _binding: FragmentPaymentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var selectedIndex = -1

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
        binding.name.setOnFocusChangeListener { view, focus ->
            var value = 0f
            val filled = activity.findViewById<EditText>(R.id.name).text.toString().isNotEmpty()
            if (focus || filled) value = -30f
            changeYpos(R.id.nameText, value)
        }
        binding.value.setOnFocusChangeListener { view, focus ->
            var value = 0f
            val filled = activity.findViewById<EditText>(R.id.value).text.toString().isNotEmpty()
            if (focus || filled) value = -30f
            changeYpos(R.id.valueText, value)
        }
        binding.date.setOnClickListener {
            hideKeyboard()
            val datePicker = DatePickerDialog(requireContext())
            datePicker.setOnDateSetListener { datePicker, year, month, day ->
                val localDate = LocalDate.of(datePicker.year,datePicker.month+1,datePicker.dayOfMonth)
                binding.date.text = Util.toNiceString(localDate)
                changeYpos(R.id.dateText, -30f)
            }
            datePicker.show()
        }
        binding.type.setOnTouchListener { view, motionEvent ->
            hideKeyboard()
            view.performClick()
        }
        ArrayAdapter(requireContext(), R.layout.spinner_item, PaymentType.values())
            .also { adapter ->
                adapter.setDropDownViewResource(R.layout.spinner_item)
                binding.type.adapter = adapter
            }
        binding.saveBtn.setOnClickListener {
            if (binding.name.text.isEmpty() ||
                binding.value.text.isEmpty() ||
                binding.date.text.isEmpty()) return@setOnClickListener
            var payment: Payment? = null
            if (selectedIndex != -1) payment = activity.payments[selectedIndex]
            if (payment == null) payment = Payment(null,null, null,null)
            payment.name = binding.name.text.toString()
            payment.value = binding.value.text.toString().toFloat()
            payment.type = PaymentType.values()[binding.type.selectedItemPosition]
            val cuttedDate = binding.date.text.toString().split('.')
            val date = LocalDate.of(cuttedDate[2].toInt(), cuttedDate[1].toInt(), cuttedDate[0].toInt())
            payment.nextTime = date
            if (selectedIndex == -1) activity.payments.add(payment)
            if (activity.payments.size > 1)
                activity.payments = PaymentUtil.sortPayments(activity.payments).toList() as ArrayList<Payment>
            activity.save()
            findNavController().navigateUp()
        }
        if(arguments != null)
            selectedIndex = arguments!!.getInt("index", -1)
        if (selectedIndex == -1) return
        binding.deleteBtn.alpha = 1f
        binding.deleteBtn.setOnClickListener {
            activity.payments.removeAt(selectedIndex)
//            findNavController().navigate(R.id.action_EntryFragment_to_OverlayFragment)
            findNavController().navigateUp()
        }
        val entry = activity.payments[selectedIndex]
        binding.name.text.insert(0, entry.name)
        changeYpos(R.id.nameText, -30f)
        var value = Util.toNiceString(entry.value!!, false)
        if (entry.value!! < 0f) value = "-$value"
        binding.value.text.insert(0, value)
        changeYpos(R.id.valueText, -30f)
        val time = entry.nextTime!!
        binding.date.text = Util.toNiceString(time)
        changeYpos(R.id.dateText, -30f)
        binding.type.setSelection(entry.type!!.ordinal)
    }

    fun hideKeyboard() {
        requireView().findFocus()
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