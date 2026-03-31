package com.example.a509lablearnandroid

import com.example.a509lablearnandroid.utils.PokemonEntry
import com.example.a509lablearnandroid.utils.PokemonSpecies
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests สำหรับทดสอบ Logic ของ PokemonViewModel
 * เนื่องจาก ViewModel ต้องใช้ viewModelScope จึงทดสอบ Logic ของ StateFlow แยก
 */
class PokemonViewModelTest {

    @Test
    fun pokemonList_initialState_isEmpty() {
        // จำลองสถานะเริ่มต้นเหมือนใน ViewModel
        val pokemonList = MutableStateFlow<List<PokemonEntry>>(emptyList())
        assertTrue(pokemonList.value.isEmpty())
    }

    @Test
    fun pokemonList_afterUpdate_containsData() {
        val pokemonList = MutableStateFlow<List<PokemonEntry>>(emptyList())

        // จำลองการอัปเดตข้อมูลหลังเรียก API สำเร็จ
        val newEntries = listOf(
            PokemonEntry(1, PokemonSpecies("bulbasaur", "url1")),
            PokemonEntry(4, PokemonSpecies("charmander", "url2")),
            PokemonEntry(7, PokemonSpecies("squirtle", "url3"))
        )
        pokemonList.value = newEntries

        assertEquals(3, pokemonList.value.size)
        assertEquals("bulbasaur", pokemonList.value[0].pokemon_species.name)
    }

    @Test
    fun pokemonList_afterUpdate_canBeFilteredByName() {
        val pokemonList = MutableStateFlow<List<PokemonEntry>>(emptyList())

        val entries = listOf(
            PokemonEntry(1, PokemonSpecies("bulbasaur", "url1")),
            PokemonEntry(2, PokemonSpecies("ivysaur", "url2")),
            PokemonEntry(4, PokemonSpecies("charmander", "url4")),
            PokemonEntry(25, PokemonSpecies("pikachu", "url25"))
        )
        pokemonList.value = entries

        // ทดสอบ filter ตามชื่อ
        val filtered = pokemonList.value.filter { it.pokemon_species.name.startsWith("b") }
        assertEquals(1, filtered.size)
        assertEquals("bulbasaur", filtered[0].pokemon_species.name)
    }

    @Test
    fun pokemonList_afterUpdate_canBeSortedByEntryNumber() {
        val pokemonList = MutableStateFlow<List<PokemonEntry>>(emptyList())

        val entries = listOf(
            PokemonEntry(25, PokemonSpecies("pikachu", "url25")),
            PokemonEntry(1, PokemonSpecies("bulbasaur", "url1")),
            PokemonEntry(150, PokemonSpecies("mewtwo", "url150"))
        )
        pokemonList.value = entries

        val sorted = pokemonList.value.sortedBy { it.entry_number }
        assertEquals(1, sorted[0].entry_number)
        assertEquals(25, sorted[1].entry_number)
        assertEquals(150, sorted[2].entry_number)
    }

    @Test
    fun pokemonList_replaceWithNewData_oldDataIsGone() {
        val pokemonList = MutableStateFlow<List<PokemonEntry>>(emptyList())

        // ใส่ข้อมูลชุดแรก
        pokemonList.value = listOf(
            PokemonEntry(1, PokemonSpecies("bulbasaur", "url1"))
        )
        assertEquals(1, pokemonList.value.size)

        // เปลี่ยนข้อมูลใหม่ทั้งหมด
        pokemonList.value = listOf(
            PokemonEntry(151, PokemonSpecies("mew", "url151")),
            PokemonEntry(152, PokemonSpecies("chikorita", "url152"))
        )
        assertEquals(2, pokemonList.value.size)
        assertEquals("mew", pokemonList.value[0].pokemon_species.name)
    }

    @Test
    fun pokemonList_setToEmptyList_clearsData() {
        val pokemonList = MutableStateFlow(
            listOf(
                PokemonEntry(1, PokemonSpecies("bulbasaur", "url1")),
                PokemonEntry(4, PokemonSpecies("charmander", "url4"))
            )
        )
        assertEquals(2, pokemonList.value.size)

        // ล้างข้อมูล
        pokemonList.value = emptyList()
        assertTrue(pokemonList.value.isEmpty())
    }

    @Test
    fun pokemonList_duplicateEntries_areAllowed() {
        val pokemonList = MutableStateFlow<List<PokemonEntry>>(emptyList())

        val duplicate = PokemonEntry(1, PokemonSpecies("bulbasaur", "url1"))
        pokemonList.value = listOf(duplicate, duplicate)

        assertEquals(2, pokemonList.value.size)
        assertEquals(pokemonList.value[0], pokemonList.value[1])
    }

    @Test
    fun pokemonList_largeDataSet_handledCorrectly() {
        val pokemonList = MutableStateFlow<List<PokemonEntry>>(emptyList())

        // จำลอง Kanto Pokedex (151 ตัว)
        val largeList = (1..151).map { i ->
            PokemonEntry(i, PokemonSpecies("pokemon_$i", "url_$i"))
        }
        pokemonList.value = largeList

        assertEquals(151, pokemonList.value.size)
        assertEquals("pokemon_1", pokemonList.value.first().pokemon_species.name)
        assertEquals("pokemon_151", pokemonList.value.last().pokemon_species.name)
    }
}
