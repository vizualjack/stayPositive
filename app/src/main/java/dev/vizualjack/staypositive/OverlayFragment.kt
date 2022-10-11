package dev.vizualjack.staypositive

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.view.*
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
    private var mainActivity: MainActivity? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOverlayBinding.inflate(inflater, container, false)
        mainActivity = activity as MainActivity
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        timelineEntries.clear()
        load()
    }

    private fun load() {
        GlobalScope.launch(Dispatchers.IO) {
            loadMore()
            withContext(Dispatchers.Main) {
                var cashText =  "${Util.toNiceString(mainActivity!!.selectedAccount!!.cash!!, true)} €"
                if (mainActivity!!.selectedAccount!!.cash!! < 0) cashText = "- ${cashText}"
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
                    mainActivity!!.selectedAccount!!.cash = input.text.toString().toFloat()
                    binding.cash.text = "${Util.toNiceString(mainActivity!!.selectedAccount!!.cash!!, true)} €"
                    mainActivity!!.save()
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
            var cash = mainActivity!!.selectedAccount!!.cash!!
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
        GlobalScope.launch(Dispatchers.IO) {
            var localDate = LocalDate.now()
            if (timelineEntries.size > 0) localDate = timelineEntries.last().currentTime.plusDays(1)
            val newEntries = PaymentUtil.createPaymentTimeline(mainActivity!!.selectedAccount!!.payments!!, localDate,20)
            timelineEntries.addAll(newEntries)
            val newViews = viewsForTimelineEntries(newEntries)
            withContext(Dispatchers.Main) {
                putEntriesInWrapper(newViews)
                isLoadingMore = false
            }
        }
    }

    private fun viewsForTimelineEntries(timelineEntries: List<PaymentTimelineEntry>): List<View> {
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
                val index = mainActivity!!.selectedAccount!!.payments!!.indexOf(timelineEntry.payment)
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_rename -> {
                renamePopup()
                true
            }
            R.id.menu_delete -> {
                deletePopup()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun renamePopup() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("AccountName")
        val input = EditText(context)
        input.setText(mainActivity!!.selectedAccount!!.name)
        builder.setView(input)
        builder.setPositiveButton("OK",
            DialogInterface.OnClickListener { dialog, which ->
                if(input.text.isEmpty()) return@OnClickListener
                val accountName = input.text.toString()
                if (mainActivity!!.selectedAccount == null) return@OnClickListener
                mainActivity!!.selectedAccount!!.name = accountName
                mainActivity!!.save()
            })
        builder.setNegativeButton("Cancel",
            DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
        builder.show()
    }

    private fun deletePopup() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Account deletion")
        val question = TextView(context)
        question.textSize = question.textSize/2.2f
        question.text = "   Do you really want to delete this account?"
        builder.setView(question)
        builder.setPositiveButton("Yes",
            DialogInterface.OnClickListener { dialog, which ->
                mainActivity!!.accounts.remove(mainActivity!!.selectedAccount!!)
                mainActivity!!.save()
                findNavController().navigateUp()
            })
        builder.setNegativeButton("No",
            DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
        builder.show()
    }
}