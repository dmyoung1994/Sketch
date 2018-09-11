package com.codely.sketch

import android.app.AlertDialog
import android.app.Fragment
import android.content.pm.ActivityInfo
import android.graphics.Canvas
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.codely.sketch.blocks.IfElseBlock
import com.codely.sketch.blocks.ModifyBlock
import com.codely.sketch.blocks.ReturnBlock
import com.codely.sketch.blocks.VarDecBlock
import java.util.ArrayList
import kotlin.concurrent.thread

class CanvasFragment : Fragment(), View.OnClickListener {
    var stateMachine = CodeStateMachine.getInstance()

    companion object {
        fun newInstance(): CanvasFragment {
            return CanvasFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater?.inflate(R.layout.canvas_main, container, false)

        // Find buttons in view
        val runButton = rootView?.findViewById<Button>(R.id.runButton)
        val varDecButton = rootView?.findViewById<Button>(R.id.varDec)
        val ifElseButton = rootView?.findViewById<Button>(R.id.ifElse)
        val printButton = rootView?.findViewById<Button>(R.id.print)
        val modifyButton = rootView?.findViewById<Button>(R.id.modifyButton)

        runButton?.setOnClickListener(this)
        varDecButton?.setOnClickListener(this)
        ifElseButton?.setOnClickListener(this)
        printButton?.setOnClickListener(this)
        modifyButton?.setOnClickListener(this)

        return rootView
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.runButton    -> handleRunButtonClick(v)
            R.id.varDec       -> handleVarDecButtonClick(v)
            R.id.ifElse       -> handleIfElseButtonClick(v)
            R.id.print        -> handlePrintButtonClick(v)
            R.id.modifyButton -> handleModifyButtonClick(v)
        }
    }

    private fun handleVarDecButtonClick(v: View) {
        val varDecDialog: AlertDialog.Builder = AlertDialog.Builder(v.context)
        val input = EditText(v.context)
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.hint = "Enter a variable name"
        varDecDialog.setView(input)
                .setTitle("Declare a variable")
                .setPositiveButton("OK") { _, _ ->
                    val varName = input.text.toString()
                    val varDecBlock = VarDecBlock(varName, v.width / 2, v.height / 2)
                    // TODO: Add error checking
                    stateMachine.varNames[varName] = varDecBlock
                    stateMachine.codeBlocks.add(varDecBlock)
                    if (stateMachine.executionBlock == null ) stateMachine.executionBlock = varDecBlock
                    input.requestFocus()
                }
                .setNegativeButton("Cancel") { d, _ ->
                    d.cancel()
                }
                .create()
                .show()
    }

    private fun handleIfElseButtonClick(v: View) {
        val ifElseDialog: AlertDialog.Builder = AlertDialog.Builder(v.context)
        val layoutGroup = LinearLayout(v.context)
        layoutGroup.orientation = LinearLayout.VERTICAL

        // Build data set for our dropdown
        val varSpinner = Spinner(v.context)
        val spinnerArray: List<String> = ArrayList(stateMachine.varNames.keys)
        val varAdapter: ArrayAdapter<String> = ArrayAdapter(v.context, android.R.layout.simple_spinner_item, spinnerArray)
        varAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        varSpinner.adapter = varAdapter
        varSpinner.prompt = "What variable should we compare?"

        // Data Field for conditionals
        val compareSpinner = Spinner(v.context)
        val compareAdapter: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(v.context, R.array.comparators, android.R.layout.simple_spinner_item)
        compareAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        compareSpinner.adapter = compareAdapter
        compareSpinner.prompt = "How should we compare it?"

        // Data field for condition target
        val input = EditText(v.context)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.hint = "What value should we compare it to?"

        layoutGroup.addView(varSpinner)
        layoutGroup.addView(compareSpinner)
        layoutGroup.addView(input)

        ifElseDialog.setView(layoutGroup)
                .setTitle("Set up a condition")
                .setPositiveButton("OK") { _, _ ->
                    // TODO: Add error checking and compare to block value
                    val conditionBlock = stateMachine.varNames[varSpinner.selectedItem.toString()]
                    val comparator = compareSpinner.selectedItem.toString()
                    val target = input.text.toString()
                    val ifElseBlock = IfElseBlock(conditionBlock!!, comparator, target, v.width/2, v.height/2)
                    stateMachine.codeBlocks.add(ifElseBlock)
                    if (stateMachine.executionBlock == null ) stateMachine.executionBlock = ifElseBlock
                }
                .setNegativeButton("Cancel") { d, _ ->
                    d.cancel()
                }
                .create()
                .show()
    }

    private fun handlePrintButtonClick(v: View) {
        val printDialog: AlertDialog.Builder = AlertDialog.Builder(v.context)
        // Build data set for our dropdown and error check
        val varSpinner = Spinner(v.context)
        val spinnerArray: List<String> = ArrayList(stateMachine.varNames.keys)
        val varAdapter: ArrayAdapter<String> = ArrayAdapter(v.context, android.R.layout.simple_spinner_item, spinnerArray)
        varAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        varSpinner.adapter = varAdapter
        varSpinner.prompt = "What do you want to print?"

        printDialog.setView(varSpinner)
                .setTitle("Print a variable")
                .setPositiveButton("OK") { _, _ ->
                    // TODO: Add error checking
                    val varBlock = stateMachine.varNames[varSpinner.selectedItem.toString()]
                    val returnBlock = ReturnBlock(varBlock!!, v.width / 2, v.height / 2)
                    stateMachine.codeBlocks.add(returnBlock)
                    if (stateMachine.executionBlock == null ) stateMachine.executionBlock = returnBlock
                }
                .setNegativeButton("Cancel") { d, _ ->
                    d.cancel()
                }
                .create()
                .show()
    }

    private fun handleRunButtonClick(v: View) {
        if (stateMachine.codeBlocks.size != 0) {
            thread(true) {
                Looper.prepare()
                stateMachine.codeBlocks.elementAt(0).run()
                val resultDialog: AlertDialog.Builder = AlertDialog.Builder(v.context)
                resultDialog.setMessage(stateMachine.terminatorBlock?.value.toString())
                        .setTitle("Code Result")
                        .show()
                Looper.loop()
            }
        } else {
            val resultDialog: AlertDialog.Builder = AlertDialog.Builder(v.context)
            resultDialog.setMessage("Drag some blocks onto the canvas to run the code!")
                    .setTitle("No Blocks!")
                    .show()
        }
    }

    private fun handleModifyButtonClick(v: View) {
        val modifyDialog: AlertDialog.Builder = AlertDialog.Builder(v.context)
        val layoutGroup = LinearLayout(v.context)
        layoutGroup.orientation = LinearLayout.VERTICAL

        // Build data set for dropdown
        val varSpinner = Spinner(v.context)
        val spinnerArray: List<String> = ArrayList(stateMachine.varNames.keys)
        val varAdapter: ArrayAdapter<String> = ArrayAdapter(v.context, android.R.layout.simple_spinner_item, spinnerArray)
        varAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        varSpinner.adapter = varAdapter
        varSpinner.prompt = "What do you want to modify?"

        // Data Field for modifiers
        val modifySpinner = Spinner(v.context)
        val modifyAdapter: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(v.context, R.array.modifiers, android.R.layout.simple_spinner_item)
        modifyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        modifySpinner.adapter = modifyAdapter
        modifySpinner.prompt = "How should we modify it?"

        // Data field for condition target
        val input = EditText(v.context)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.hint = "By what?"

        layoutGroup.addView(varSpinner)
        layoutGroup.addView(modifySpinner)
        layoutGroup.addView(input)

        modifyDialog.setView(layoutGroup)
                .setTitle("Set up a condition")
                .setPositiveButton("OK") { _, _ ->
                    // TODO: Error Checking
                    val varBlock = stateMachine.varNames[varSpinner.selectedItem.toString()]
                    val modifier = modifySpinner.selectedItem.toString()
                    val value = input.text.toString().toInt()
                    val modifyBlock = ModifyBlock(varBlock!!, modifier, value, v.width/2, v.height/2)
                    stateMachine.codeBlocks.add(modifyBlock)
                    if (stateMachine.executionBlock == null ) stateMachine.executionBlock = modifyBlock
                }
                .setNegativeButton("Cancel") { d, _ ->
                    d.cancel()
                }
                .create()
                .show()
    }
}
