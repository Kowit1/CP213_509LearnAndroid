package com.example.a509lablearnandroid

import com.example.a509lablearnandroid.utils.PokemonNetwork
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests สำหรับ PokemonNetwork Singleton
 * ทดสอบการสร้าง Retrofit instance และความเป็น Singleton
 */
class PokemonNetworkTest {

    @Test
    fun pokemonNetwork_api_isNotNull() {
        // ทดสอบว่า API instance ถูกสร้างสำเร็จ
        val api = PokemonNetwork.api
        assertNotNull(api)
    }

    @Test
    fun pokemonNetwork_api_isSingleton() {
        // ทดสอบว่าเรียกกี่ครั้งก็ได้ instance เดิม (Singleton pattern)
        val api1 = PokemonNetwork.api
        val api2 = PokemonNetwork.api
        assertSame(api1, api2)
    }
}
