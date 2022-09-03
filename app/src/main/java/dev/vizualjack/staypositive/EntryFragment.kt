package dev.vizualjack.staypositive

import android.animation.ObjectAnimator
import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.findNavController
import dev.vizualjack.staypositive.databinding.FragmentEntryBinding
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.temporal.TemporalField
import java.util.Calendar

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class EntryFragment : Fragment() {

    private var _binding: FragmentEntryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var selectedIndex = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEntryBinding.inflate(inflater, container, false)
        return binding.root

    }

    @RequiresApi(Build.VERSION_CODES.N)
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
            val datePicker = DatePickerDialog(requireContext())
            datePicker.setOnDateSetListener { datePicker, year, month, day ->
                binding.date.text = "${datePicker.dayOfMonth}.${datePicker.month+1}.${datePicker.year}"
                changeYpos(R.id.dateText, -30f)
            }
            datePicker.show()
        }
        ArrayAdapter(requireContext(), R.layout.spinner_item, EntryType.values())
            .also { adapter ->
                adapter.setDropDownViewResource(R.layout.spinner_item)
                binding.type.adapter = adapter
            }
        binding.saveBtn.setOnClickListener {
            if (binding.name.text.isEmpty() ||
                binding.value.text.isEmpty() ||
                binding.date.text.isEmpty()) return@setOnClickListener

            var entry: Entry? = null
            if (selectedIndex != -1) entry = activity.entries[selectedIndex]
            if (entry == null) entry = Entry(null,null, null,null)
            entry.name = binding.name.text.toString()
            entry.value = binding.value.text.toString().toFloat()
            entry.type = EntryType.values()[binding.type.selectedItemPosition]
            val cuttedDate = binding.date.text.toString().split('.')
            val date = LocalDate.of(cuttedDate[2].toInt(), cuttedDate[1].toInt(), cuttedDate[0].toInt())
            entry.startTime = date
            if (selectedIndex == -1) activity.entries.add(entry)
            findNavController().navigate(R.id.action_EntryFragment_to_OverlayFragment)
        }
        if(arguments != null)
            selectedIndex = arguments!!.getInt("index", -1)
        if (selectedIndex == -1) return
        binding.deleteBtn.alpha = 1f
        binding.deleteBtn.setOnClickListener {
            activity.entries.removeAt(selectedIndex)
            findNavController().navigate(R.id.action_EntryFragment_to_OverlayFragment)
        }
//        binding.name.text.insert(0,activity.entries[selectedIndex])
//        Fill components with selected entry values
        val entry = activity.entries[selectedIndex]
        binding.name.text.insert(0, entry.name)
        changeYpos(R.id.nameText, -30f)
        binding.value.text.insert(0, entry.value.toString())
        changeYpos(R.id.valueText, -30f)
        val time = entry.startTime!!
        binding.date.text = "${time.dayOfMonth!!}.${time.monthValue}.${time.year}"
        changeYpos(R.id.dateText, -30f)
        binding.type.setSelection(entry.type!!.ordinal)
    }

    fun changeYpos(id: Int, newY: Float) {
        ObjectAnimator.ofFloat(requireActivity().findViewById(id), "translationY", newY).apply {
            duration = 1000
            start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}