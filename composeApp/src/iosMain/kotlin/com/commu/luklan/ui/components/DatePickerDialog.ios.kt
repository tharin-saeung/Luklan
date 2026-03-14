package com.commu.luklan.ui.components

import androidx.compose.runtime.*
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectMake
import platform.Foundation.*
import platform.UIKit.*

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun DatePickerDialog(
    initialDate: String,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    LaunchedEffect(Unit) {
        // Get the root view controller
        val window = UIApplication.sharedApplication.keyWindow
        val rootViewController = window?.rootViewController
        
        if (rootViewController != null) {
            // Create a UIAlertController
            val alertController = UIAlertController.alertControllerWithTitle(
                title = "เลือกวันหมดอายุ",
                message = "\n\n\n\n\n\n\n\n\n\n",  // Space for the picker
                preferredStyle = UIAlertControllerStyleAlert
            )
            
            // Create and configure the date picker
            val datePicker = UIDatePicker().apply {
                datePickerMode = UIDatePickerMode.UIDatePickerModeDate
                preferredDatePickerStyle = UIDatePickerStyle.UIDatePickerStyleWheels
                
                // Set initial date
                if (initialDate.isNotEmpty()) {
                    try {
                        val formatter = NSDateFormatter().apply {
                            dateFormat = "yyyy-MM-dd"
                        }
                        formatter.dateFromString(initialDate)?.let { nsDate ->
                            setDate(nsDate)
                        }
                    } catch (e: Exception) {
                        // Use current date
                    }
                }
                
                // Set frame
                setFrame(CGRectMake(0.0, 50.0, 270.0, 200.0))
            }
            
            // Add date picker to alert controller
            alertController.view.addSubview(datePicker)
            
            // Cancel button
            val cancelAction = UIAlertAction.actionWithTitle(
                title = "ยกเลิก",
                style = UIAlertActionStyleCancel,
                handler = { _ ->
                    onDismiss()
                }
            )
            alertController.addAction(cancelAction)
            
            // OK button
            val okAction = UIAlertAction.actionWithTitle(
                title = "ตกลง",
                style = UIAlertActionStyleDefault,
                handler = { _ ->
                    val formatter = NSDateFormatter().apply {
                        dateFormat = "yyyy-MM-dd"
                    }
                    val dateString = formatter.stringFromDate(datePicker.date)
                    onDateSelected(dateString)
                }
            )
            alertController.addAction(okAction)
            
            // Present the alert
            rootViewController.presentViewController(
                viewControllerToPresent = alertController,
                animated = true,
                completion = null
            )
        } else {
            // Fallback: just call onDismiss
            onDismiss()
        }
    }
}

private fun getCurrentDate(): String {
    val formatter = NSDateFormatter().apply {
        dateFormat = "yyyy-MM-dd"
    }
    return formatter.stringFromDate(NSDate())
}
