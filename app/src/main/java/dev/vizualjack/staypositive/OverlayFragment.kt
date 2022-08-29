package dev.vizualjack.staypositive

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dev.vizualjack.staypositive.databinding.FragmentOverlayBinding
import org.w3c.dom.Attr


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class OverlayFragment : Fragment() {

    private var _binding: FragmentOverlayBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentOverlayBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = activity as MainActivity
        val linearLayout = view.findViewById<LinearLayout>(R.id.entriesWrapper)

        for (entry in activity.entries) {
            val view = OverlayEntryView(requireContext())
            view.layout(0,0,0,0)
            linearLayout.addView(view)
        }

        binding.cashEditBtn.setOnClickListener { view ->
            val builder = AlertDialog.Builder(context)
            builder.setTitle("New today cash")
            val input = EditText(context)
            input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            builder.setView(input)
            builder.setPositiveButton("OK",
                DialogInterface.OnClickListener { dialog, which ->
                    binding.cash.text = input.text.toString()
                })
            builder.setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
            builder.show()
        }

        binding.fab.setOnClickListener { view ->
            findNavController().navigate(R.id.action_OverlayFragment_to_EntryFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}