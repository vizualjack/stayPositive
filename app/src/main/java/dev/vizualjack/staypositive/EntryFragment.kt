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
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.findNavController
import dev.vizualjack.staypositive.databinding.FragmentEntryBinding

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
        binding.saveBtn.setOnClickListener {
            if (selectedIndex == -1) activity.entries.add(binding.name.text.toString())
            else activity.entries[selectedIndex] = binding.name.text.toString()
            findNavController().navigate(R.id.action_EntryFragment_to_OverlayFragment)
        }
        binding.date.setOnClickListener {
            val datePicker = DatePickerDialog(requireContext())
            datePicker.setOnDateSetListener { datePicker, year, month, day ->
                binding.date.text = "${datePicker.dayOfMonth}.${datePicker.month}.${datePicker.year}"
            }
            datePicker.show()
        }
        ArrayAdapter.createFromResource(requireContext(), R.array.entry_type, R.layout.spinner_item)
            .also { adapter ->
                adapter.setDropDownViewResource(R.layout.spinner_item)
                binding.spinner.adapter = adapter
            }

        binding.value.setOnFocusChangeListener { view, b ->
//            println("focus: $b")
//            println("moovin")
//            binding.textView.translationX += 5

            ObjectAnimator.ofFloat(activity.findViewById(R.id.valueText), "translationX", 100f).apply {
                duration = 2000
                start()
            }
        }

        if(arguments != null)
            selectedIndex = arguments!!.getInt("index", -1)
        println(selectedIndex)
        if (selectedIndex == -1) return
        binding.name.text.insert(0,activity.entries[selectedIndex])
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}