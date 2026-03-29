/*
 * Scheduled Messages Constants - Default templates
 */
package com.sponsorflow.nexus.scheduler

/**
 * Constantes y plantillas para mensajes programados
 */
object ScheduledMessagesConstants {

    // Plantillas profesionales (ventas)
    val DEFAULT_TEMPLATES = listOf(
        // Seguimiento de lead
        MessageTemplate(
            id = "followup_1",
            type = ScheduledMessageType.FOLLOWUP_LEAD,
            name = "Seguimiento - 1 día",
            template = "Hola {{name}}, te escribo para saber si tienes alguna duda sobre {{product}}. ¿Te gustaría proceder con la compra?",
            variables = listOf("name", "product")
        ),
        MessageTemplate(
            id = "followup_2",
            type = ScheduledMessageType.FOLLOWUP_LEAD,
            name = "Seguimiento - 3 días",
            template = "Hola {{name}}, quería saber si ya pudiste decidir sobre {{product}}. Si necesitas más información, estoy aquí para ayudarte.",
            variables = listOf("name", "product")
        ),
        MessageTemplate(
            id = "followup_3",
            type = ScheduledMessageType.FOLLOWUP_LEAD,
            name = "Último intento - 7 días",
            template = "Hola {{name}}, entiendo que quizás no sea el momento. Te dejo mi contacto por si en el futuro necesitas algo. Que tengas un buen día!",
            variables = listOf("name")
        ),

        // Recordatorio de pago
        MessageTemplate(
            id = "payment_1",
            type = ScheduledMessageType.PAYMENT_REMINDER,
            name = "Recordatorio pago - 1 día",
            template = "Hola {{name}}, te recuerdo que tienes un pago pendiente de $'{{amount}} por {{product}}. ¿Podrías confirmar el pago?",
            variables = listOf("name", "amount", "product")
        ),
        MessageTemplate(
            id = "payment_2",
            type = ScheduledMessageType.PAYMENT_REMINDER,
            name = "Recordatorio pago - 3 días",
            template = "Hola {{name}}, aún tenemos el pago pendiente de $'{{amount}}. Por favor, confirmanos cuando puedas. El producto está reservado por 48hrs.",
            variables = listOf("name", "amount")
        ),

        // Post-venta
        MessageTemplate(
            id = "post_sale_1",
            type = ScheduledMessageType.POST_PURCHASE,
            name = "Gracias por comprar",
            template = "Hola {{name}}, gracias por tu compra de {{product}}! 🎉 Te llegara en {{delivery_days}} días. Cualquier duda, aquí estoy.",
            variables = listOf("name", "product", "delivery_days")
        ),
        MessageTemplate(
            id = "post_sale_2",
            type = ScheduledMessageType.POST_PURCHASE,
            name = "Seguimiento - 3 días",
            template = "Hola {{name}}, ya recibiste tu {{product}}? Esperamos que te Encante! Déjanos saber si tienes alguna duda.",
            variables = listOf("name", "product")
        ),
        MessageTemplate(
            id = "post_sale_3",
            type = ScheduledMessageType.POST_PURCHASE,
            name = "Review request",
            template = "Hola {{name}}, si ya recibiste tu {{product}}, te agradeceríamos una opinión! Te regalamos 10% de descuento para tu próxima compra.",
            variables = listOf("name", "product")
        ),

        // Promoción
        MessageTemplate(
            id = "promo_1",
            type = ScheduledMessageType.PROMOTION,
            name = "Descuento por tiempo limitado",
            template = "Hola {{name}}! 🎁 Solo por hoy, {{discount}} de descuento en {{product}}. Usa el código: {{code}}. Expira en 24hrs!",
            variables = listOf("name", "discount", "product", "code")
        ),
        MessageTemplate(
            id = "promo_2",
            type = ScheduledMessageType.PROMOTION,
            name = "Cliente inactivo",
            template = "Hola {{name}}, te extrañamos! Hace {{days}} días que no nos visitas. Te mandamos un código de descuento especial para ti: {{code}}",
            variables = listOf("name", "days", "code")
        )
    )

    // Keys para SharedPreferences
    const val PREF_MESSAGES = "scheduled_messages"
    const val PREF_TEMPLATES = "message_templates"
}
