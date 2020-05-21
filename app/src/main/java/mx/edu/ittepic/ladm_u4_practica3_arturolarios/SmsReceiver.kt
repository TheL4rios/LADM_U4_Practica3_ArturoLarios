package mx.edu.ittepic.ladm_u4_practica3_arturolarios

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.telephony.SmsMessage
import com.google.firebase.firestore.FirebaseFirestore

class SmsReceiver : BroadcastReceiver()
{
    object DB{
        const val COLLECTION = "Lender"
    }

    object SMS{
        const val ERROR_SYNTAX = "error1"
        const val ERROR_NUMBER = "error2"
        const val ERROR_TYPE = "error3"

        const val SYNTAX = "CONSULTA"
        const val DEBIT = "SALDO"
        const val DATE = "FECHA"
        const val DEBIT_DATE = "SALDO-FECHA"
    }

    override fun onReceive(context: Context?, intent: Intent?)
    {
        val extras = intent?.extras

        extras?.let { e ->
            val sms = e.get("pdus") as Array<*>

            for (index in sms.indices)
            {
                val format = e.getString("format")
                val smsMessage =
                    SmsMessage.createFromPdu(sms[index] as ByteArray, format)

                val originPhone = smsMessage.originatingAddress.toString()
                val message = smsMessage.messageBody.toString()

                analyzeSms(originPhone, message)
            }
        }
    }

    private fun analyzeSms(originPhone: String, message: String)
    {
        if (!message.contains(" "))
        {
            sendMessage(originPhone, SMS.ERROR_SYNTAX)
            return
        }

        val syntaxMessage = message.split(" ") as ArrayList<String>

        if (syntaxMessage.size != 3)
        {
            sendMessage(originPhone, SMS.ERROR_SYNTAX)
            return
        }

        if (syntaxMessage[0] != SMS.SYNTAX)
        {
            sendMessage(originPhone, SMS.ERROR_SYNTAX)
            return
        }

        getData(originPhone, syntaxMessage[1], syntaxMessage[2])
    }

    private fun getData(originPhone : String, phone : String, type : String)
    {
        val db = FirebaseFirestore.getInstance().collection(DB.COLLECTION).document(phone)

        when(type)
        {
            SMS.DEBIT -> {
                db.get()
                    .addOnSuccessListener {
                        val debit = it.getDouble("debitBalance")

                        if (debit == null)
                        {
                            sendMessage(originPhone, SMS.ERROR_NUMBER)
                            return@addOnSuccessListener
                        }

                        val message = "Usted tiene un Adeudo de: $$debit"
                        sendMessage(originPhone, message)
                    }
            }
            SMS.DATE -> {
                db.get()
                    .addOnSuccessListener {
                        val date = it.getTimestamp("limitDate")?.toDate()

                        if (date == null)
                        {
                            sendMessage(originPhone, SMS.ERROR_NUMBER)
                            return@addOnSuccessListener
                        }

                        val dateFormat = "${date.day}/" +
                                         "${date.month}/" +
                                            date.year.toString().substring(1, 3)

                        val message = "Usted tiene hasta el día $dateFormat para pagar."
                        sendMessage(originPhone, message)
                    }
            }
            SMS.DEBIT_DATE -> {
                db.get()
                    .addOnSuccessListener {
                        val date = it.getTimestamp("limitDate")?.toDate()

                        if (date == null)
                        {
                            sendMessage(originPhone, SMS.ERROR_NUMBER)
                            return@addOnSuccessListener
                        }

                        val dateFormat = "${date.day}/" +
                                         "${date.month}/" +
                                            date.year.toString().substring(1, 3)

                        val message = "Usted tiene un adeudo de $${it.getDouble("debitBalance")} y " +
                                      "tiene hasta el día $dateFormat para pagar"

                        sendMessage(originPhone, message)
                    }
            }
            else -> sendMessage(originPhone, SMS.ERROR_TYPE)
        }
    }

    private fun sendMessage(originPhone : String, message : String)
    {
        when(message)
        {
            SMS.ERROR_SYNTAX -> {
                val messageError = "Error de sintaxis, favor de verificarla. Recuerde que la sintaxis correcta es\n" +
                                   "CONSULTA NumeroTelefono LoQueDeseaConsultar: Ejemplo ->\n" +
                                   "CONSULTA 3111536699 SALDO"

                SmsManager.getDefault().sendTextMessage(originPhone, null, messageError, null, null)
            }
            SMS.ERROR_NUMBER -> {
                val messageError = "Error de sintaxis, Número incorrecto, favor de verificarlo"

                SmsManager.getDefault().sendTextMessage(originPhone, null, messageError, null, null)
            }
            SMS.ERROR_TYPE -> {
                val messageError = "Error de sintaxis, Tipo incorrecto. Recuerde que solo existen \n->SALDO" +
                                   "\n->FECHA\n->SALDO-FECHA"

                SmsManager.getDefault().sendTextMessage(originPhone, null, messageError, null, null)
            }
            else -> {
                SmsManager.getDefault().sendTextMessage(originPhone, null, message, null, null)
            }
        }
    }
}