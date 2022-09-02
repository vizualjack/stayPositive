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
            ObjectAnimator.ofFloat(activity.findViewById(R.id.nameText), "translationY", value).apply {
                duration = 1000
                start()
            }
        }
        binding.value.setOnFocusChangeListener { view, focus ->
            var value = 0f
            val filled = activity.findViewById<EditText>(R.id.value).text.toString().isNotEmpty()
            if (focus || filled) value = -30f
            ObjectAnimator.ofFloat(activity.findViewById(R.id.valueText), "translationY", value).apply {
                duration = 1000
                start()
            }
        }
        binding.date.setOnClickListener {
            val datePicker = DatePickerDialog(requireContext())
            datePicker.setOnDateSetListener { datePicker, year, month, day ->
                binding.date.text = "${datePicker.dayOfMonth}.${datePicker.month}.${datePicker.year}"
                ObjectAnimator.ofFloat(activity.findViewById(R.id.dateText), "translationY", -30f).apply {
                    duration = 1000
                    start()
                }
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
            if (entry == null) entry = Entry(null,null,null,null)
            entry.name = binding.name.text.toString()
            entry.value = binding.value.text.toString().toFloat()
            entry.type = EntryType.values()[binding.type.selectedItemPosition]
            val cuttedDate = binding.date.text.toString().split('.')
            val date = LocalDate.of(cuttedDate[2].toInt(), cuttedDate[1].toInt()+1, cuttedDate[0].toInt())
            entry.startTime = date
            activity.entries.add(entry)
            findNavController().navigate(R.id.action_EntryFragment_to_OverlayFragment)
        }
        if(arguments != null)
            selectedIndex = arguments!!.getInt("index", -1)
        if (selectedIndex == -1) return
//        binding.name.text.insert(0,activity.entries[selectedIndex])
//        Fill components with selected entry values
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}