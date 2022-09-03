package dev.vizualjack.staypositive

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dev.vizualjack.staypositive.databinding.FragmentOverlayBinding
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileReader


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

        for ((index, entry) in activity.entries.withIndex()) {
            val newEntry = LayoutInflater.from(context).inflate(R.layout.fragment_overlay_entry, null)
            val textView = newEntry.findViewById<TextView>(R.id.overlay_entry_cashView)
            textView.text = entry.name
            newEntry.setOnClickListener {
                val bundle = bundleOf("index" to index)
                findNavController().navigate(R.id.action_OverlayFragment_to_EntryFragment, bundle)
            }
            newEntry.setPadding(10,10,10,10)
            linearLayout.addView(newEntry)
        }
        binding.cash.text = "${activity.todayCash} €"
        binding.cash.setOnClickListener { view ->
            val builder = AlertDialog.Builder(context)
            builder.setTitle("New today cash")
            val input = EditText(context)
            input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            builder.setView(input)
            builder.setPositiveButton("OK",
                DialogInterface.OnClickListener { dialog, which ->
                    activity.todayCash = input.text.toString().toFloat()
                    binding.cash.text = "${activity.todayCash} €"
                    activity.save()
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