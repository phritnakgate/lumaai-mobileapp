package org.bkkz.lumaapp.util.component.chat_history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.bkkz.lumaapp.R

class ReadAllHistoryBottomSheet : BottomSheetDialogFragment() {

    private lateinit var closeBtn : ImageView
    private lateinit var txtViewFullText : TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.read_all_bottomsheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findView()
        setupView()
        setupEvents()
    }

    private fun findView(){
        closeBtn = requireView().findViewById(R.id.imgview_bottomsheet_readall_close)
        txtViewFullText = requireView().findViewById(R.id.txtview_bottomsheet_readall_body)
    }

    private fun setupView(){
        val fullText = arguments?.getString(ARG_FULL_TEXT)
        txtViewFullText.text = fullText
    }

    private fun setupEvents(){
        closeBtn.setOnClickListener {
            dismiss()
        }
    }

    companion object {
        private const val ARG_FULL_TEXT = "full_text"

        fun newInstance(fullText: String): ReadAllHistoryBottomSheet {
            val fragment = ReadAllHistoryBottomSheet()
            val args = Bundle()
            args.putString(ARG_FULL_TEXT, fullText)
            fragment.arguments = args
            return fragment
        }
    }
}