package dev.vizualjack.staypositive

import android.app.AlertDialog
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
import androidx.core.view.get
import androidx.core.view.setMargins
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dev.vizualjack.staypositive.databinding.FragmentOverlayBinding
import kotlinx.coroutines.*
import java.time.LocalDate


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class OverlayFragment : Fragment() {

    private var _binding: FragmentOverlayBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var timelineEntries = ArrayList<PaymentTimelineEntry>()
    private var entryHeight = 0
    private var previousIndex = 0
    private var isLoadingMore = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOverlayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        timelineEntries.clear()
        load()
    }

    private fun load() {
        val activity = activity as MainActivity
        GlobalScope.launch(Dispatchers.IO) {
            activity.todayCash = PaymentUtil.calculateTillDate(LocalDate.now(), activity.payments, activity.todayCash, false)
            activity.save()
            loadMore()
            withContext(Dispatchers.Main) {
                var cashText =  "${Util.toNiceString(activity.todayCash, true)} €"
                if (activity.todayCash < 0) cashText = "- ${cashText}"
                binding.cash.text = cashText
            }
        }
        binding.cash.setOnClickListener { view ->
            val builder = AlertDialog.Builder(context)
            builder.setTitle("New today cash")
            val input = EditText(context)
            input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            builder.setView(input)
            builder.setPositiveButton("OK",
                DialogInterface.OnClickListener { dialog, which ->
                    if(input.text.isEmpty()) return@OnClickListener
                    activity.todayCash = input.text.toString().toFloat()
                    binding.cash.text = "${Util.toNiceString(activity.todayCash, true)} €"
                    activity.save()
                })
            builder.setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
            builder.show()
        }
        binding.fab.setOnClickListener { view ->
            findNavController().navigate(R.id.action_OverlayFragment_to_EntryFragment)
        }
        binding.entries.setOnScrollChangeListener { view, i, currentHeight, i3, previousHeight ->
            val linearLayout = requireView().findViewById<LinearLayout>(R.id.entriesWrapper)
            if (entryHeight == 0) {
                val firstEntry = linearLayout.get(0)
                entryHeight = firstEntry.height
                val marginLayout = firstEntry.layoutParams as ViewGroup.MarginLayoutParams
                entryHeight += marginLayout.topMargin + marginLayout.bottomMargin
            }
            val currentIndex = currentHeight / entryHeight
            val triggerHeight = linearLayout.height - (view.height*2)
            if (currentHeight >= triggerHeight) loadMore()
            if(currentIndex == previousIndex) return@setOnScrollChangeListener
            previousIndex = currentIndex
            var cash = activity.todayCash
            for (i in 0 until currentIndex) {
                cash += timelineEntries[i].payment.value!!
            }
            var cashText =  "${Util.toNiceString(cash, true)} €"
            if (cash < 0) cashText = "- ${cashText}"
            binding.cash.text = cashText
        }
    }

    private fun loadMore() {
        if (isLoadingMore) return
        isLoadingMore = true
        val activity = activity as MainActivity
        GlobalScope.launch(Dispatchers.IO) {
            var localDate = LocalDate.now()
            if (timelineEntries.size > 0) localDate = timelineEntries.last().currentTime.plusDays(1)
            val newEntries = PaymentUtil.createPaymentTimeline(activity.payments, localDate,20)
            timelineEntries.addAll(newEntries)
            val newViews = viewsForTimelineEntries(newEntries)
            withContext(Dispatchers.Main) {
                putEntriesInWrapper(newViews)
                isLoadingMore = false
            }
        }
    }

    private fun viewsForTimelineEntries(timelineEntries: List<PaymentTimelineEntry>): List<View> {
        val activity = activity as MainActivity
        val views = ArrayList<View>()
        for (timelineEntry in timelineEntries) {
            val newEntry = LayoutInflater.from(context).inflate(R.layout.fragment_overlay_payment, null)
            val nameView = newEntry.findViewById<TextView>(R.id.overlay_entry_nameView)
            val dateView = newEntry.findViewById<TextView>(R.id.overlay_entry_dateView)
            val cashView = newEntry.findViewById<TextView>(R.id.overlay_entry_cashView)
            nameView.text = timelineEntry.payment.name
            val time = timelineEntry.currentTime
            dateView.text = Util.toNiceString(time)
            var cashViewText = Util.toNiceString(timelineEntry.payment.value!!, true)
            var preSign = "+"
            var colorId = R.color.green
            if (timelineEntry.payment.value!! < 0f) {
                preSign = "-"
                colorId = R.color.red
            }
            cashView.text = "$preSign $cashViewText €"
            cashView.setTextColor(resources.getColor(colorId, null))
            newEntry.setOnClickListener {
                val index = activity.payments.indexOf(timelineEntry.payment)
                val bundle = bundleOf("index" to index)
                findNavController().navigate(R.id.action_OverlayFragment_to_EntryFragment, bundle)
            }
            views.add(newEntry)
        }
        return views
    }

    private fun putEntriesInWrapper(views: List<View>) {
        val linearLayout = requireView().findViewById<LinearLayout>(R.id.entriesWrapper)
        for (view in views) {
            linearLayout.addView(view)
            view.layoutParams.height = 100 * requireContext().resources.displayMetrics.density.toInt()
            val marginLayout = view.layoutParams as ViewGroup.MarginLayoutParams
            marginLayout.setMargins(30)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}