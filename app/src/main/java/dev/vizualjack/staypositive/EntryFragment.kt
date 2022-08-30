package dev.vizualjack.staypositive

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = activity as MainActivity
        binding.saveBtn.setOnClickListener {
            if (selectedIndex == -1) activity.entries.add(binding.entryText.text.toString())
            else activity.entries[selectedIndex] = binding.entryText.text.toString()
            findNavController().navigate(R.id.action_EntryFragment_to_OverlayFragment)
        }
        if(arguments != null)
            selectedIndex = arguments!!.getInt("index", -1)
        println(selectedIndex)
        if (selectedIndex == -1) return
        binding.entryText.text.insert(0,activity.entries[selectedIndex])
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}