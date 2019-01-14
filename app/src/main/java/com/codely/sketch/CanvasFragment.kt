package com.codely.sketch

import android.app.AlertDialog
import android.support.v4.app.Fragment
import android.os.Bundle
import android.os.Looper
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.codely.sketch.blocks.*
import java.util.ArrayList
import kotlin.concurrent.thread

class CanvasFragment : Fragment(), View.OnClickListener {
    private var stateMachine = CodeStateMachine.getInstance()
    private var typesEnabled = false
    private var buttonTypes: List<CodeButton> = ArrayList()

    companion object {
        fun newInstance(typesEnabled: Boolean, buttonTypes: List<CodeButton> = ArrayList()): CanvasFragment {
            val fragment = CanvasFragment()
            val bundle = Bundle()
            val buttonIntArray = ArrayList(buttonTypes.map { it -> it.ordinal })
            bundle.putIntegerArrayList("buttons", buttonIntArray)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.canvas_main, container, false)

        // Generate buttons from list passed in
        val buttonsSubset: ArrayList<Int> = arguments!!.getIntegerArrayList("buttons")!!
        val buttonContainer = rootView.findViewById<LinearLayout>(R.id.buttonContainer)
        val runButton = rootView.findViewById<Button>(R.id.runButton)

        if (buttonsSubset.size == 0) {
            createAllButtons(buttonContainer)
        } else {
            for((index, button) in buttonsSubset.withIndex()) {
                val codeButtonType = CodeButton.fromInt(button)!!
                buttonContainer.addView(createButton(index, codeButtonType))
            }
        }

        runButton.setOnClickListener { handleRunButtonClick(it) }

        return rootView
    }

    private fun createAllButtons(container: LinearLayout) {
        for((index, codeButton) in CodeButton.values().withIndex()) {
            container.addView(createButton(index, codeButton))
        }
    }

    private fun createButton(index: Int, type: CodeButton) : Button {
        val button = Button(context)
        button.id = index
        when (type) {
            CodeButton.VAR_DEC -> {
                button.setText(R.string.var_dec_block)
                button.setOnClickListener { handleVarDecButtonClick(it) }
            }

            CodeButton.IF_ELSE -> {
                button.setText(R.string.if_else)
                button.setOnClickListener { handleIfElseButtonClick(it) }
            }

            CodeButton.MODIFY -> {
                button.setText(R.string.Modify)
                button.setOnClickListener { handleModifyButtonClick(it) }
            }

            CodeButton.PRINT -> {
                button.setText(R.string.Print)
                button.setOnClickListener { handlePrintButtonClick(it) }
            }
        }

        return button
    }

    private fun handleVarDecButtonClick(v: View) {
        val varDecDialog: AlertDialog.Builder = AlertDialog.Builder(v.context)
        val layoutGroup = LinearLayout(v.context)
        layoutGroup.orientation = LinearLayout.VERTICAL

        // For declaring a variable name
        val varName = EditText(v.context)
        varName.inputType = InputType.TYPE_CLASS_TEXT
        varName.hint = "Enter a variable name"

        // TODO: Make these only appear after previous has been entered
        // Build data set for our dropdown
        val typeSpinner = Spinner(v.context)
        val typeAdapter: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(v.context, R.array.comparators, android.R.layout.simple_spinner_item)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        typeSpinner.adapter = typeAdapter
        typeSpinner.prompt = "What type should it be?"

        val initValue = EditText(v.context)
        initValue.inputType = InputType.TYPE_CLASS_TEXT
        initValue.hint = "Set an initial value"

        layoutGroup.addView(varName)
        layoutGroup.addView(typeSpinner)
        layoutGroup.addView(initValue)

        varDecDialog.setView(layoutGroup)
            .setTitle("Declare a variable")
            .setPositiveButton("OK") { _, _ ->
                val varName = varName.text.toString()
                val varType = VarType.fromString(typeSpinner.selectedItem.toString())
                val varDecBlock = VarDecBlock(varName, v.width / 2, v.height / 2, varType)
                // TODO: Add error checking
                stateMachine.varNames[varName] = varDecBlock
                stateMachine.codeBlocks.add(varDecBlock)
                if (stateMachine.executionBlock == null ) stateMachine.executionBlock = varDecBlock
                layoutGroup.requestFocus()
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

    // Not really sure what to do with this...
    override fun onClick(v: View?) {
    }
}
