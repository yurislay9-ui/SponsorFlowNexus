/**
 * SponsorFlow Nexus v1.0 - Contact DAO Test
 * 
 * Test de instrumentaciÃ³n para validar el comportamiento del DAO de contactos
 * con Room Database. Este test asegura que las operaciones CRUD en la base de datos
 * funcionen correctamente en un entorno Android real.
 * 
 * @author SponsorFlow Nexus Team
 * @version 1.0
 * @since 1.0
 */
package com.sponsorflow.nexus

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sponsorflow.nexus.data.dao.ContactDao
import com.sponsorflow.nexus.data.entity.ContactEntity
import com.sponsorflow.nexus.data.database.NexusDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test de instrumentaciÃ³n para ContactDao.
 * 
 * Este test valida las operaciones CRUD del DAO de contactos utilizando
 * una base de datos en memoria para pruebas. Asegura que todas las operaciones
 * de base de datos funcionen correctamente en un entorno Android real.
 * 
 * @see ContactEntity
 * @see NexusDatabase
 */
@RunWith(AndroidJUnit4::class)
class ContactDaoTest {
    
    private lateinit var database: NexusDatabase
    private lateinit var dao: ContactDao
    
    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, NexusDatabase::class.java)
            .allowMainThreadQueries() // Para tests, normalmente usar coroutines
            .build()
        dao = database.contactDao()
    }
    
    @After
    fun tearDown() {
        database.close()
    }
    
    /**
     * Test para insertar y recuperar un contacto.
     * 
     * Verifica que un contacto pueda ser insertado en la base de datos
     * y recuperado correctamente por su nÃºmero de telÃ©fono.
     */
    @Test
    fun insertAndRetrieve() = runBlocking {
        // Given
        val contact = ContactEntity(
            phone = "+1234567890",
            name = "Test Contact",
            lastMessage = "Hello World",
            lastMessageTime = System.currentTimeMillis(),
            isBlocked = false,
            messageCount = 5,
            lastInteraction = System.currentTimeMillis()
        )
        
        // When
        dao.insert(contact)
        val result = dao.getByPhone("+1234567890")
        
        // Then
        assertNotNull("Contact should be found", result)
        assertEquals("Name should match", "Test Contact", result?.name)
        assertEquals("Phone should match", "+1234567890", result?.phone)
        assertEquals("Message count should match", 5, result?.messageCount)
    }
    
    /**
     * Test para actualizar un contacto existente.
     * 
     * Verifica que un contacto existente pueda ser actualizado
     * y que los cambios se reflejen correctamente en la base de datos.
     */
    @Test
    fun updateContact() = runBlocking {
        // Given
        val contact = ContactEntity(
            phone = "+1234567891",
            name = "Original Name",
            lastMessage = "Original Message",
            lastMessageTime = System.currentTimeMillis(),
            isBlocked = false,
            messageCount = 0,
            lastInteraction = System.currentTimeMillis()
        )
        
        dao.insert(contact)
        
        // When
        val updatedContact = contact.copy(
            name = "Updated Name",
            lastMessage = "Updated Message",
            messageCount = 10
        )
        dao.update(updatedContact)
        val result = dao.getByPhone("+1234567891")
        
        // Then
        assertNotNull("Contact should be found", result)
        assertEquals("Name should be updated", "Updated Name", result?.name)
        assertEquals("Message should be updated", "Updated Message", result?.lastMessage)
        assertEquals("Message count should be updated", 10, result?.messageCount)
    }
    
    /**
     * Test para bloquear/desbloquear un contacto.
     * 
     * Verifica que el estado de bloqueo de un contacto pueda ser cambiado
     * y que el cambio se refleje correctamente en la base de datos.
     */
    @Test
    fun toggleBlockContact() = runBlocking {
        // Given
        val contact = ContactEntity(
            phone = "+1234567892",
            name = "Block Test",
            lastMessage = "Test Message",
            lastMessageTime = System.currentTimeMillis(),
            isBlocked = false,
            messageCount = 0,
            lastInteraction = System.currentTimeMillis()
        )
        
        dao.insert(contact)
        
        // When - Block contact
        dao.setBlocked("+1234567892", true)
        val blockedResult = dao.getByPhone("+1234567892")
        
        // Then - Verify blocked
        assertNotNull("Contact should be found", blockedResult)
        assertTrue("Contact should be blocked", blockedResult?.isBlocked == true)
        
        // When - Unblock contact
        dao.setBlocked("+1234567892", false)
        val unblockedResult = dao.getByPhone("+1234567892")
        
        // Then - Verify unblocked
        assertNotNull("Contact should be found", unblockedResult)
        assertFalse("Contact should be unblocked", unblockedResult?.isBlocked == true)
    }
    
    /**
     * Test para incrementar el contador de mensajes.
     * 
     * Verifica que el contador de mensajes de un contacto pueda ser
     * incrementado correctamente.
     */
    @Test
    fun incrementMessageCount() = runBlocking {
        // Given
        val contact = ContactEntity(
            phone = "+1234567893",
            name = "Message Test",
            lastMessage = "Test",
            lastMessageTime = System.currentTimeMillis(),
            isBlocked = false,
            messageCount = 5,
            lastInteraction = System.currentTimeMillis()
        )
        
        dao.insert(contact)
        
        // When
        dao.incrementMessageCount("+1234567893")
        val result = dao.getByPhone("+1234567893")
        
        // Then
        assertNotNull("Contact should be found", result)
        assertEquals("Message count should be incremented", 6, result?.messageCount)
    }
    
    /**
     * Test para obtener contactos bloqueados.
     * 
     * Verifica que se puedan obtener correctamente todos los contactos
     * que estÃ¡n actualmente bloqueados.
     */
    @Test
    fun getBlockedContacts() = runBlocking {
        // Given
        val contact1 = ContactEntity(
            phone = "+1234567894",
            name = "Blocked Contact 1",
            lastMessage = "Test",
            lastMessageTime = System.currentTimeMillis(),
            isBlocked = true,
            messageCount = 0,
            lastInteraction = System.currentTimeMillis()
        )
        
        val contact2 = ContactEntity(
            phone = "+1234567895",
            name = "Blocked Contact 2",
            lastMessage = "Test",
            lastMessageTime = System.currentTimeMillis(),
            isBlocked = true,
            messageCount = 0,
            lastInteraction = System.currentTimeMillis()
        )
        
        val contact3 = ContactEntity(
            phone = "+1234567896",
            name = "Unblocked Contact",
            lastMessage = "Test",
            lastMessageTime = System.currentTimeMillis(),
            isBlocked = false,
            messageCount = 0,
            lastInteraction = System.currentTimeMillis()
        )
        
        dao.insert(contact1)
        dao.insert(contact2)
        dao.insert(contact3)
        
        // When
        val blockedContacts = dao.getBlockedContacts()
        
        // Then
        assertEquals("Should have 2 blocked contacts", 2, blockedContacts.size)
        assertTrue("Should contain blocked contact 1", 
            blockedContacts.any { it.phone == "+1234567894" })
        assertTrue("Should contain blocked contact 2", 
            blockedContacts.any { it.phone == "+1234567895" })
        assertFalse("Should not contain unblocked contact", 
            blockedContacts.any { it.phone == "+1234567896" })
    }
    
    /**
     * Test para obtener contactos por rango de tiempo.
     * 
     * Verifica que se puedan obtener contactos que han interactuado
     * dentro de un rango de tiempo especÃ­fico.
     */
    @Test
    fun getContactsByTimeRange() = runBlocking {
        // Given
        val now = System.currentTimeMillis()
        val oneHourAgo = now - (60 * 60 * 1000) // 1 hora atrÃ¡s
        val twoHoursAgo = now - (2 * 60 * 60 * 1000) // 2 horas atrÃ¡s
        
        val recentContact = ContactEntity(
            phone = "+1234567897",
            name = "Recent Contact",
            lastMessage = "Recent",
            lastMessageTime = now,
            isBlocked = false,
            messageCount = 0,
            lastInteraction = now
        )
        
        val oldContact = ContactEntity(
            phone = "+1234567898",
            name = "Old Contact",
            lastMessage = "Old",
            lastMessageTime = twoHoursAgo,
            isBlocked = false,
            messageCount = 0,
            lastInteraction = twoHoursAgo
        )
        
        dao.insert(recentContact)
        dao.insert(oldContact)
        
        // When
        val contactsInRange = dao.getContactsByTimeRange(oneHourAgo, now)
        
        // Then
        assertEquals("Should have 1 contact in time range", 1, contactsInRange.size)
        assertEquals("Should be the recent contact", "+1234567897", contactsInRange[0].phone)
    }
    
    /**
     * Test para eliminar un contacto.
     * 
     * Verifica que un contacto pueda ser eliminado de la base de datos
     * y que ya no estÃ© disponible para recuperaciÃ³n.
     */
    @Test
    fun deleteContact() = runBlocking {
        // Given
        val contact = ContactEntity(
            phone = "+1234567899",
            name = "Delete Test",
            lastMessage = "Test",
            lastMessageTime = System.currentTimeMillis(),
            isBlocked = false,
            messageCount = 0,
            lastInteraction = System.currentTimeMillis()
        )
        
        dao.insert(contact)
        var result = dao.getByPhone("+1234567899")
        assertNotNull("Contact should exist before deletion", result)
        
        // When
        dao.deleteByPhone("+1234567899")
        result = dao.getByPhone("+1234567899")
        
        // Then
        assertNull("Contact should not exist after deletion", result)
    }
    
    /**
     * Test para obtener estadÃ­sticas de contactos.
     * 
     * Verifica que las estadÃ­sticas generales de contactos (total, bloqueados, etc.)
     * puedan ser calculadas correctamente.
     */
    @Test
    fun getContactStatistics() = runBlocking {
        // Given
        val totalContacts = listOf(
            ContactEntity(phone = "+1234567900", name = "Contact 1", isBlocked = false, messageCount = 5, lastInteraction = System.currentTimeMillis()),
            ContactEntity(phone = "+1234567901", name = "Contact 2", isBlocked = true, messageCount = 3, lastInteraction = System.currentTimeMillis()),
            ContactEntity(phone = "+1234567902", name = "Contact 3", isBlocked = false, messageCount = 8, lastInteraction = System.currentTimeMillis()),
            ContactEntity(phone = "+1234567903", name = "Contact 4", isBlocked = true, messageCount = 2, lastInteraction = System.currentTimeMillis()),
            ContactEntity(phone = "+1234567904", name = "Contact 5", isBlocked = false, messageCount = 1, lastInteraction = System.currentTimeMillis())
        )
        
        totalContacts.forEach { dao.insert(it) }
        
        // When
        val total = dao.getTotalContacts()
        val blocked = dao.getBlockedContacts().size
        val active = total - blocked
        
        // Then
        assertEquals("Total contacts should be 5", 5, total)
        assertEquals("Blocked contacts should be 2", 2, blocked)
        assertEquals("Active contacts should be 3", 3, active)
    }
    
    /**
     * Test para manejar contactos con nombres nulos.
     * 
     * Verifica que el sistema pueda manejar contactos con nombres nulos
     * sin causar errores o comportamientos inesperados.
     */
    @Test
    fun handleNullNameContact() = runBlocking {
        // Given
        val contact = ContactEntity(
            phone = "+1234567905",
            name = null, // Nombre nulo
            lastMessage = "Test with null name",
            lastMessageTime = System.currentTimeMillis(),
            isBlocked = false,
            messageCount = 0,
            lastInteraction = System.currentTimeMillis()
        )
        
        // When
        dao.insert(contact)
        val result = dao.getByPhone("+1234567905")
        
        // Then
        assertNotNull("Contact should be found", result)
        assertNull("Name should be null", result?.name)
        assertEquals("Phone should match", "+1234567905", result?.phone)
    }
}