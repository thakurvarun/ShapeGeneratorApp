package com.example.myapplication.ui.main

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.databinding.MainActivityBinding
import com.example.myapplication.model.ShapeAndColorJson
import com.example.myapplication.utils.Utils
import com.google.gson.Gson

/**
 * ShapeGeneratorActivity : A class to create all the Shapes for a specified color.
 */
class ShapeGeneratorActivity : AppCompatActivity() {

    private lateinit var mBindedView: MainActivityBinding
    private var mSelectedShape: String? = null
    private var mSelectedColor : String? = null
    private var mSelectedColorIndex: Int  = -1
    private var mShapeAndColorJson: ShapeAndColorJson? = null
    private var mBuilder :AlertDialog.Builder? = null
    private var mAlertDialog: AlertDialog? = null
    private var mIsComingFromFirstLaunch: Boolean = true
    private val mStrokeWidth: Float = 5F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBindedView = MainActivityBinding.inflate(layoutInflater)
        val view = mBindedView.root
        setContentView(view)
        val jsonFileString = Utils.getJsonDataFromAsset(applicationContext, "shapesAndColor.json")
        val gson = Gson()
        //Treating this Will be the Json Response
        mShapeAndColorJson = gson.fromJson(jsonFileString, ShapeAndColorJson::class.java)
        initViews()
    }

    /**
     * Init the Views
     */
    private fun initViews( ) {
        initializeShapesSpinner()
        initializeColorSpinner()
        initializeGenerateShape()
    }

    /**
     * Initialize the Shapes Spinner
     * Rectangle, Square, Circle etc
     */
    private fun initializeShapesSpinner() {
        // Initializing an ArrayAdapter for shapes Spinner
        val adapter = ArrayAdapter(
            this, // Context
            android.R.layout.simple_spinner_item, // Layout
            getShapes() // Array
        )
        // Set the drop down view resource
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        // Bind the data of spinner object with adapter
        mBindedView.spinnerShape.adapter = adapter;
        // Set an on item selected listener for spinner object
        mBindedView.spinnerShape.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent:AdapterView<*>?, view: View?, position: Int, id: Long){
                mBindedView.imageShape.setImageBitmap(null)
                mSelectedShape = parent!!.getItemAtPosition(position).toString()
                when (mSelectedShape) {
                    "Rectangle" -> {
                        showRectangleViews()
                    }
                    "Square" -> {
                        showSquareViews()
                    }
                    "Circle" -> {
                        showCircleViews()
                    }
                    "Triangle" ->  {
                        //Not fully implemented
                        showTriangleViews()
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>){
                // Another interface callback
            }
        }
    }

    /**
     * Intialize the Colors Spinner
     * Red, Purple, Green, Yellow etc.
     */
    private fun initializeColorSpinner() {

        // Initializing an ArrayAdapter for Color Spinner
        val adapterColors = ArrayAdapter(
            this, // Context
            android.R.layout.simple_spinner_item, // Layout
            getColors() // Array
        )
        // Set the drop down view resource
        adapterColors.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        // Finally, data bind the spinner object with adapter
        mBindedView.spinnerColor.adapter = adapterColors;
        // Set an on item selected listener for spinner object
        mBindedView.spinnerColor.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent:AdapterView<*>, view: View, position: Int, id: Long){
                mSelectedColor = parent.getItemAtPosition(position).toString()
                mSelectedColorIndex = position
                applyShapeAndColorChanges()
            }
            override fun onNothingSelected(parent: AdapterView<*>){
                // Another interface callback
            }
        }
    }

    /**
     * Intialize the Generate Shape Button and add a onClickListener.
     */
    private fun initializeGenerateShape() {
        mBindedView.buttonGenerateShape.setOnClickListener(onGenerateShapeClickListener)
    }

    /**
     * onGenerateShapeClickListener : On Click Listner
     */
    private val onGenerateShapeClickListener= View.OnClickListener { view ->
        when (view.getId()) {
            mBindedView.buttonGenerateShape.id -> {
                applyShapeAndColorChanges()
                closeKeyBoard()
            }
        }
    }

    /**
     * Business Logic for Applying all the shape and color changes to the selected values in Shapes Spinner
     * and Color Spinner.
     */
    private fun applyShapeAndColorChanges () {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        var width = displayMetrics.widthPixels
        var height = displayMetrics.widthPixels

        when(mSelectedShape) {
            "Rectangle" -> {
                val lengthText = mBindedView.editLength.text.toString()
                val widthText = mBindedView.editWidth.text.toString()
                if(!TextUtils.isEmpty(lengthText) && !TextUtils.isEmpty(widthText) &&
                    lengthText.toInt() <= height &&
                    widthText.toInt() <= width ) {
                        if(lengthText.toInt() <=0 || widthText.toInt() <= 0) {
                            mBindedView.textViewColor.visibility = View.GONE
                            mBindedView.spinnerColor.visibility = View.GONE
                            mBindedView.imageShape.setImageBitmap(null)
                            showErrorDialog(resources.getString(R.string.height_width_greater_than_bitmap_image_width_error_msg, height.toString(), width.toString()))
                            return
                        }
                    mBindedView.textViewColor.visibility = View.VISIBLE
                    mBindedView.spinnerColor.visibility = View.VISIBLE
                    // draw circle on canvas and get bitmap
                    val bitmap = drawRectangleorSquare(
                        color = android.graphics.Color.parseColor(
                            getColorCodes().get(
                                mSelectedColorIndex
                            ).toString()
                        ),
                        canvasBackground = android.R.color.white,
                        bitmapWidth = width,
                        bitmapHeight = height,
                        rectangleHeight = lengthText.toInt(),
                        rectangleWidth = widthText.toInt(),
                        strokeWidth = mStrokeWidth,
                        strokeColor = Color.parseColor(getStrokeColorCodes().get(mSelectedColorIndex).toString()),
                    )
                    mBindedView.imageShape.setImageBitmap(bitmap)
                } else {
                    mBindedView.textViewColor.visibility = View.GONE
                    mBindedView.spinnerColor.visibility = View.GONE
                    mBindedView.imageShape.setImageBitmap(null)
                    if(TextUtils.isEmpty(lengthText) && !mIsComingFromFirstLaunch) {
                        showErrorDialog(resources.getString(R.string.enter_length_error_msg))
                    } else  if(TextUtils.isEmpty(widthText) && !mIsComingFromFirstLaunch) {
                        showErrorDialog(resources.getString(R.string.enter_width_error_msg))

                    } else if (((!TextUtils.isEmpty(lengthText) && lengthText.toInt()> height) || (!TextUtils.isEmpty(widthText) && widthText.toInt() > width)) && !mIsComingFromFirstLaunch) {
                        showErrorDialog(resources.getString(R.string.height_width_greater_than_bitmap_image_width_error_msg, height.toString(), width.toString()))
                    } else {
                        if(mIsComingFromFirstLaunch) {
                        mIsComingFromFirstLaunch = false
                }
                    }
                }

            }
            "Square" -> {
                val lengthText = mBindedView.editLength.text.toString()
                if(!TextUtils.isEmpty(lengthText) && !TextUtils.isEmpty(lengthText) && lengthText.toInt() <= width ) {

                    if(lengthText.toInt() <=0 ) {
                        mBindedView.textViewColor.visibility = View.GONE
                        mBindedView.spinnerColor.visibility = View.GONE
                        mBindedView.imageShape.setImageBitmap(null)
                        showErrorDialog(resources.getString(R.string.height_or_width_greater_than_bitmap_image_width_error_msg, width.toString()))
                        return
                    }
                    mBindedView.textViewColor.visibility = View.VISIBLE
                    mBindedView.spinnerColor.visibility = View.VISIBLE
                    // draw circle on canvas and get bitmap
                    val bitmap = drawRectangleorSquare(
                        color = android.graphics.Color.parseColor(
                            getColorCodes().get(
                                mSelectedColorIndex
                            ).toString()
                        ),
                        canvasBackground = android.graphics.Color.parseColor("#FFFFFF"),
                        bitmapWidth = width,
                        bitmapHeight = height,
                        rectangleHeight = lengthText.toInt(),
                        rectangleWidth = lengthText.toInt(),
                        strokeWidth = mStrokeWidth,
                        strokeColor = Color.parseColor(getStrokeColorCodes().get(mSelectedColorIndex).toString()),
                        )
                    mBindedView.imageShape.setImageBitmap(bitmap)
                } else {
                    mBindedView.textViewColor.visibility = View.GONE
                    mBindedView.spinnerColor.visibility = View.GONE
                    mBindedView.imageShape.setImageBitmap(null)
                    if(TextUtils.isEmpty(lengthText)) {
                        showErrorDialog(resources.getString(R.string.enter_length_width_error_msg))

                    } else if(lengthText.toInt() > width) {
                        showErrorDialog(resources.getString(R.string.height_or_width_greater_than_bitmap_image_width_error_msg, width.toString()))
                    }
                }
            }

            "Circle" -> {
                val radiusText = mBindedView.editRadius.text.toString()
                // draw circle on canvas and get bitmap
                if(!TextUtils.isEmpty(radiusText) && radiusText.toInt() <= width/2) {
                    if(radiusText.toInt() <=0 ) {
                        mBindedView.textViewColor.visibility = View.GONE
                        mBindedView.spinnerColor.visibility = View.GONE
                        mBindedView.imageShape.setImageBitmap(null)
                        showErrorDialog(resources.getString(R.string.radius_greater_than_half_of_width_error_msg, (width/2 - mStrokeWidth.toInt()).toString()))
                        return
                    }
                    mBindedView.textViewColor.visibility = View.VISIBLE
                    mBindedView.spinnerColor.visibility = View.VISIBLE
                    val bitmap = drawCircle(
                        color = android.graphics.Color.parseColor(
                            getColorCodes().get(
                                mSelectedColorIndex
                            ).toString()
                        ),
                        radius = radiusText.toFloat(),
                        canvasBackground = android.R.color.white,
                        bitmapWidth = width,
                        bitmapHeight = height,
                        strokeWidth = mStrokeWidth,
                        strokeColor = Color.parseColor(getStrokeColorCodes().get(mSelectedColorIndex).toString()),
                        )
                    mBindedView.imageShape.setImageBitmap(bitmap)
                } else {
                    mBindedView.textViewColor.visibility = View.GONE
                    mBindedView.spinnerColor.visibility = View.GONE
                    mBindedView.imageShape.setImageBitmap(null)
                    if (TextUtils.isEmpty(radiusText)) {
                        showErrorDialog(resources.getString(R.string.enter_radius_error_msg))
                    } else if(radiusText.toInt() > width/2-mStrokeWidth.toInt()) {
                        showErrorDialog(resources.getString(R.string.radius_greater_than_half_of_width_error_msg, (width/2 - mStrokeWidth.toInt()).toString()))
                    }
                }
            }

            "Triangle" -> {
                val lengthText = mBindedView.editLength.text.toString()
                if(!TextUtils.isEmpty(lengthText) && !TextUtils.isEmpty(lengthText) && lengthText.toInt() <= width ) {
                    mBindedView.textViewColor.visibility = View.VISIBLE
                    mBindedView.spinnerColor.visibility = View.VISIBLE
                    // draw circle on canvas and get bitmap
                    // draw triangle on canvas
                    val bitmap = drawTriangle(
                        sideLength = lengthText.toFloat(),
                        yOffsetTopPoint = 150F,
                        xOffsetTopPoint = 750F,
                        xOffsetLeftPoint = 200F,
                        yOffsetBottomHand = 150F,
                        strokeWidth = mStrokeWidth,
                        strokeColor = Color.parseColor(getStrokeColorCodes().get(mSelectedColorIndex).toString()),
                        fillColor = Color.parseColor(getColorCodes().get(mSelectedColorIndex).toString()),
                        canvasColor = android.R.color.white
                    )
                    mBindedView.imageShape.setImageBitmap(bitmap)
                } else {
                    mBindedView.textViewColor.visibility = View.GONE
                    mBindedView.spinnerColor.visibility = View.GONE
                    mBindedView.imageShape.setImageBitmap(null)
                    if(TextUtils.isEmpty(lengthText)) {
                        showErrorDialog(resources.getString(R.string.enter_side_length_of_triangle_error_msg))
                    } else if(lengthText.toInt() > 500) {
                        showErrorDialog(resources.getString(R.string.height_or_width_greater_than_bitmap_image_width_error_msg, "500"))
                    }
                }
            }
        }
    }

    /**
     * Method to draw a Circle
     */
    private fun drawCircle(
        color:Int = android.graphics.Color.LTGRAY,
        bitmapWidth:Int ,
        bitmapHeight:Int ,
        radius: Float,
        cx:Float = bitmapWidth / 2f,
        cy:Float = bitmapHeight / 2f,
        canvasBackground:Int = Color.WHITE,
        strokeWidth : Float = mStrokeWidth,
        strokeColor : Int = Color.BLACK,
        ):Bitmap{
        val bitmap = Bitmap.createBitmap(
            bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888
        )
        // canvas to draw circle
        val canvas = Canvas(bitmap).apply {
            drawColor(canvasBackground)
        }
        // paint to draw on canvas
        val paint = Paint().apply {
            this.color = color
            isAntiAlias = true
            this.strokeWidth = strokeWidth
            this.style = Paint.Style.FILL
        }
        // draw circle on canvas
        canvas.drawCircle(
            cx, // x-coordinate of the center of the circle
            cy, // y-coordinate of the center of the circle
            radius, // radius of the circle
            paint // paint used to draw the circle
        )
        // change paint color to draw rectangle/Square border
        paint.apply {
            this.color = strokeColor
            style = Paint.Style.STROKE
        }
        // draw circle on canvas
        canvas.drawCircle(
            cx, // x-coordinate of the center of the circle
            cy, // y-coordinate of the center of the circle
            radius, // radius of the circle
            paint // paint used to draw the circle
        )
        return bitmap
    }

    /**
     * Method to draw a Rectangle
     */
    private fun drawRectangleorSquare(
        color:Int = android.graphics.Color.LTGRAY,
        bitmapWidth:Int ,
        bitmapHeight:Int ,
        canvasBackground:Int = android.graphics.Color.WHITE,
        rectangleWidth : Int = 100,
        rectangleHeight : Int = 100,
        strokeWidth : Float = mStrokeWidth,
        strokeColor : Int = Color.BLACK,
        ):Bitmap{
        val bitmap = Bitmap.createBitmap(
            bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888
        )
        // canvas to draw circle
        val canvas = Canvas(bitmap).apply {
            drawColor(canvasBackground)
        }
        // paint to draw on canvas
        val paint = Paint().apply {
            this.color = color
            isAntiAlias = true
            this.strokeWidth = strokeWidth
            this.style = Paint.Style.FILL
        }
        val centerOfCanvas = Point(bitmapWidth / 2, bitmapHeight / 2)
        val rectW = rectangleWidth
        val rectH = rectangleHeight
        val left: Int = centerOfCanvas.x - rectW / 2
        val top: Int = centerOfCanvas.y - rectH / 2
        val right: Int = centerOfCanvas.x + rectW / 2
        val bottom: Int = centerOfCanvas.y + rectH / 2
        val rect = Rect(left, top, right, bottom)
        canvas.drawRect(rect, paint)
        // change paint color to draw rectangle/Square border
        paint.apply {
            this.color = strokeColor
            style = Paint.Style.STROKE
        }
        canvas.drawRect(rect, paint)
        return bitmap
    }


    /**
     * Method to draw a Triangle
     * Not Fully Implemented
     */
    private fun drawTriangle(
        sideLength : Float = 600F,
        yOffsetTopPoint : Float = 100F,
        xOffsetTopPoint : Float = 600F,
        xOffsetLeftPoint : Float = 200F,
        yOffsetBottomHand : Float = 150F,
        strokeWidth : Float = mStrokeWidth,
        strokeColor : Int = Color.BLACK,
        fillColor : Int = Color.CYAN,
        canvasColor : Int = Color.LTGRAY
    ):Bitmap?{
        val bitmap = Bitmap.createBitmap(
            1500,
            850,
            Bitmap.Config.ARGB_8888
        )
        // canvas to draw triangle
        val canvas = Canvas(bitmap).apply {
            drawColor(canvasColor)
        }
        // paint to draw triangle fill color
        val paint = Paint().apply {
            isAntiAlias = true
            color = fillColor
            this.strokeWidth = strokeWidth
            style = Paint.Style.FILL
        }
        // create a path to draw triangle
        val path = Path().apply {
            fillType = Path.FillType.EVEN_ODD
            // draw rectangle on canvas
            moveTo(xOffsetLeftPoint, canvas.height - yOffsetBottomHand)
            lineTo(xOffsetTopPoint, yOffsetTopPoint)
            lineTo(xOffsetTopPoint  + sideLength, canvas.height - yOffsetBottomHand)
            close()
        }
        // draw path on canvas
        // it will draw triangle fill color
        canvas.drawPath(path, paint)

        // change paint color to draw triangle border
        paint.apply {
            color = strokeColor
            style = Paint.Style.STROKE
        }
        // it will draw triangle border on canvas
        canvas.drawPath(path,paint)
        return bitmap
    }

    /**
     * Get Colors array from ShapesAndColorJson file.
     */
    private fun getColors(): Array<CharSequence> {
        return mShapeAndColorJson!!.colors.map{ it.color }.toTypedArray()
    }

    /**
     * Get ColorCodes array from ShapesAndColorJson file.
     */
    private fun getColorCodes(): Array<CharSequence> {
        return mShapeAndColorJson!!.colors.map { it.color_code }.toTypedArray()
    }

    /**
     * Get StrokeColors array from ShapesAndColorJson file.
     */
    private fun getStrokeColorCodes(): Array<CharSequence> {
        return mShapeAndColorJson!!.colors.map { it.stroke_color_code }.toTypedArray()
    }

    /**
     * Get Shapes array from ShapesAndColorJson file.
     */
    private fun getShapes(): Array<CharSequence> {
        return mShapeAndColorJson!!.shapes.map { it.shape }.toTypedArray()
    }

    /**
     * Show Error Dialog while Validations applied on the GenerateShape Button Click.
     */
    private fun showErrorDialog(error_message : String) {
        dismissErrorDialog() // Dissmiss if already showing
        mBuilder = AlertDialog.Builder(this)
        //set title for alert dialog
        mBuilder?.setTitle(R.string.error_dialog_title)
        //set message for alert dialog
        mBuilder?.setMessage(error_message)
        mBuilder?.setIcon(android.R.drawable.ic_dialog_alert)
        //performing positive action
        mBuilder?.setPositiveButton(resources.getString(R.string.Ok)){ dialogInterface, which ->
            dialogInterface.dismiss()
        }
        // Create the AlertDialog
        mAlertDialog = mBuilder?.create()
        // Set other dialog properties
        mAlertDialog?.setCancelable(false)
        mAlertDialog?.show()
    }

    /**
     * Dismiss the Alert Dialog, if already showing.
     */
    private fun dismissErrorDialog () {
        if(mAlertDialog != null && mAlertDialog!!.isShowing) {
            mAlertDialog?.dismiss()
        }
    }

    /**
     * Show/Hide Rectangle Views.
     */
    private fun showRectangleViews() {
        mBindedView.length.text = resources.getString(com.example.myapplication.R.string.length)
        mBindedView.length.visibility = View.VISIBLE
        mBindedView.width.visibility = View.VISIBLE
        mBindedView.editLength.visibility = View.VISIBLE
        mBindedView.editWidth.visibility = View.VISIBLE
        mBindedView.editRadius.visibility = View.GONE
        mBindedView.radius.visibility = View.GONE
    }

    /**
     * Show/Hide Square Views.
     */
    private fun showSquareViews() {
        mBindedView.length.text = resources.getString(com.example.myapplication.R.string.length_or_width)
        mBindedView.editLength.hint = resources.getString(R.string.length_or_width_hint)
        mBindedView.length.visibility = View.VISIBLE
        mBindedView.width.visibility = View.GONE
        mBindedView.editLength.visibility = View.VISIBLE
        mBindedView.editWidth.visibility = View.GONE
        mBindedView.editRadius.visibility = View.GONE
        mBindedView.radius.visibility = View.GONE
    }

    /**
     * Show/Hide Circle Views.
     */
    private fun showCircleViews() {
        mBindedView.length.visibility = View.GONE
        mBindedView.width.visibility = View.GONE
        mBindedView.editLength.visibility = View.GONE
        mBindedView.editWidth.visibility = View.GONE
        mBindedView.editRadius.visibility = View.VISIBLE
        mBindedView.radius.visibility = View.VISIBLE
    }

    /**
     * Show/Hide Triangle Views.
     */
    private fun showTriangleViews() {
        mBindedView.length.text = resources.getString(com.example.myapplication.R.string.side_length)
        mBindedView.editLength.hint = resources.getString(R.string.side_length_hint)
        mBindedView.length.visibility = View.VISIBLE
        mBindedView.width.visibility = View.GONE
        mBindedView.editLength.visibility = View.VISIBLE
        mBindedView.editWidth.visibility = View.GONE
        mBindedView.editRadius.visibility = View.GONE
        mBindedView.radius.visibility = View.GONE
        mBindedView.editLength.setText("500")
    }


    /**
     * A function to Hide the Keyboard when clicked on Generate Shape button.
     */
    private fun closeKeyBoard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}