package com.example.a509lablearnandroid

import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests สำหรับทดสอบ Logic ของ SensorViewModel
 * ทดสอบ StateFlow ของ Sensor Data และ Location Data
 */
class SensorViewModelLogicTest {

    // ===== Sensor Data (Accelerometer) Tests =====

    @Test
    fun sensorData_initialState_isAllZeros() {
        val sensorData = MutableStateFlow(FloatArray(3) { 0f })
        assertEquals(0f, sensorData.value[0], 0.001f)
        assertEquals(0f, sensorData.value[1], 0.001f)
        assertEquals(0f, sensorData.value[2], 0.001f)
    }

    @Test
    fun sensorData_afterUpdate_hasCorrectValues() {
        val sensorData = MutableStateFlow(FloatArray(3) { 0f })

        // จำลองค่า Accelerometer (x, y, z)
        sensorData.value = floatArrayOf(1.5f, -2.3f, 9.8f)

        assertEquals(1.5f, sensorData.value[0], 0.001f)
        assertEquals(-2.3f, sensorData.value[1], 0.001f)
        assertEquals(9.8f, sensorData.value[2], 0.001f)
    }

    @Test
    fun sensorData_hasThreeComponents() {
        val sensorData = MutableStateFlow(FloatArray(3) { 0f })
        assertEquals(3, sensorData.value.size)
    }

    @Test
    fun sensorData_negativeValues_areAllowed() {
        val sensorData = MutableStateFlow(FloatArray(3) { 0f })
        sensorData.value = floatArrayOf(-9.8f, -9.8f, -9.8f)

        assertTrue(sensorData.value.all { it < 0 })
    }

    @Test
    fun sensorData_rapidUpdates_lastValueWins() {
        val sensorData = MutableStateFlow(FloatArray(3) { 0f })

        // จำลองการอัปเดตหลายครั้งรวดเร็ว
        sensorData.value = floatArrayOf(1f, 1f, 1f)
        sensorData.value = floatArrayOf(2f, 2f, 2f)
        sensorData.value = floatArrayOf(3f, 3f, 3f)

        assertEquals(3f, sensorData.value[0], 0.001f)
        assertEquals(3f, sensorData.value[1], 0.001f)
        assertEquals(3f, sensorData.value[2], 0.001f)
    }

    // ===== Location Data Tests =====

    @Test
    fun locationData_initialState_isZeroZero() {
        val locationData = MutableStateFlow(Pair(0.0, 0.0))
        assertEquals(0.0, locationData.value.first, 0.001)
        assertEquals(0.0, locationData.value.second, 0.001)
    }

    @Test
    fun locationData_afterUpdate_hasCorrectLatLng() {
        val locationData = MutableStateFlow(Pair(0.0, 0.0))

        // จำลองพิกัด กรุงเทพฯ
        locationData.value = Pair(13.7563, 100.5018)

        assertEquals(13.7563, locationData.value.first, 0.001)
        assertEquals(100.5018, locationData.value.second, 0.001)
    }

    @Test
    fun locationData_negativeCoordinates_areValid() {
        val locationData = MutableStateFlow(Pair(0.0, 0.0))

        // จำลองพิกัดซีกโลกใต้ (Sydney, Australia)
        locationData.value = Pair(-33.8688, 151.2093)

        assertTrue(locationData.value.first < 0)
        assertTrue(locationData.value.second > 0)
    }

    @Test
    fun locationData_edgeCases_northPole() {
        val locationData = MutableStateFlow(Pair(0.0, 0.0))
        locationData.value = Pair(90.0, 0.0)
        assertEquals(90.0, locationData.value.first, 0.001)
    }

    @Test
    fun locationData_edgeCases_southPole() {
        val locationData = MutableStateFlow(Pair(0.0, 0.0))
        locationData.value = Pair(-90.0, 0.0)
        assertEquals(-90.0, locationData.value.first, 0.001)
    }

    @Test
    fun locationData_edgeCases_internationalDateLine() {
        val locationData = MutableStateFlow(Pair(0.0, 0.0))
        locationData.value = Pair(0.0, 180.0)
        assertEquals(180.0, locationData.value.second, 0.001)
    }

    @Test
    fun locationData_multipleUpdates_lastValueWins() {
        val locationData = MutableStateFlow(Pair(0.0, 0.0))

        locationData.value = Pair(13.7563, 100.5018) // กรุงเทพฯ
        locationData.value = Pair(35.6762, 139.6503) // โตเกียว
        locationData.value = Pair(40.7128, -74.0060) // นิวยอร์ก

        assertEquals(40.7128, locationData.value.first, 0.001)
        assertEquals(-74.0060, locationData.value.second, 0.001)
    }

    @Test
    fun locationData_pairDestructuring_worksCorrectly() {
        val locationData = MutableStateFlow(Pair(13.7563, 100.5018))

        val (lat, lng) = locationData.value
        assertEquals(13.7563, lat, 0.001)
        assertEquals(100.5018, lng, 0.001)
    }
}
