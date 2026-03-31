package com.example.a509lablearnandroid

import com.example.a509lablearnandroid.utils.PokemonEntry
import com.example.a509lablearnandroid.utils.PokemonSpecies
import com.example.a509lablearnandroid.utils.PokedexResponse
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests สำหรับ Data Model ของ Pokemon
 * ทดสอบ Data Class: PokemonSpecies, PokemonEntry, PokedexResponse
 */
class PokemonDataModelTest {

    // ===== PokemonSpecies Tests =====

    @Test
    fun pokemonSpecies_creation_hasCorrectName() {
        val species = PokemonSpecies(name = "bulbasaur", url = "https://pokeapi.co/api/v2/pokemon-species/1/")
        assertEquals("bulbasaur", species.name)
    }

    @Test
    fun pokemonSpecies_creation_hasCorrectUrl() {
        val species = PokemonSpecies(name = "bulbasaur", url = "https://pokeapi.co/api/v2/pokemon-species/1/")
        assertEquals("https://pokeapi.co/api/v2/pokemon-species/1/", species.url)
    }

    @Test
    fun pokemonSpecies_equality_sameValues_areEqual() {
        val species1 = PokemonSpecies(name = "pikachu", url = "https://pokeapi.co/api/v2/pokemon-species/25/")
        val species2 = PokemonSpecies(name = "pikachu", url = "https://pokeapi.co/api/v2/pokemon-species/25/")
        assertEquals(species1, species2)
    }

    @Test
    fun pokemonSpecies_equality_differentName_areNotEqual() {
        val species1 = PokemonSpecies(name = "pikachu", url = "https://pokeapi.co/api/v2/pokemon-species/25/")
        val species2 = PokemonSpecies(name = "raichu", url = "https://pokeapi.co/api/v2/pokemon-species/25/")
        assertNotEquals(species1, species2)
    }

    @Test
    fun pokemonSpecies_equality_differentUrl_areNotEqual() {
        val species1 = PokemonSpecies(name = "pikachu", url = "https://pokeapi.co/api/v2/pokemon-species/25/")
        val species2 = PokemonSpecies(name = "pikachu", url = "https://pokeapi.co/api/v2/pokemon-species/26/")
        assertNotEquals(species1, species2)
    }

    @Test
    fun pokemonSpecies_copy_createsIndependentCopy() {
        val original = PokemonSpecies(name = "charmander", url = "https://pokeapi.co/api/v2/pokemon-species/4/")
        val copy = original.copy(name = "charmeleon")
        assertEquals("charmeleon", copy.name)
        assertEquals("charmander", original.name) // original ไม่เปลี่ยน
    }

    @Test
    fun pokemonSpecies_emptyName_isAllowed() {
        val species = PokemonSpecies(name = "", url = "")
        assertEquals("", species.name)
        assertEquals("", species.url)
    }

    // ===== PokemonEntry Tests =====

    @Test
    fun pokemonEntry_creation_hasCorrectEntryNumber() {
        val species = PokemonSpecies(name = "bulbasaur", url = "https://pokeapi.co/api/v2/pokemon-species/1/")
        val entry = PokemonEntry(entry_number = 1, pokemon_species = species)
        assertEquals(1, entry.entry_number)
    }

    @Test
    fun pokemonEntry_creation_hasCorrectSpecies() {
        val species = PokemonSpecies(name = "bulbasaur", url = "https://pokeapi.co/api/v2/pokemon-species/1/")
        val entry = PokemonEntry(entry_number = 1, pokemon_species = species)
        assertEquals("bulbasaur", entry.pokemon_species.name)
    }

    @Test
    fun pokemonEntry_equality_sameValues_areEqual() {
        val species = PokemonSpecies(name = "squirtle", url = "https://pokeapi.co/api/v2/pokemon-species/7/")
        val entry1 = PokemonEntry(entry_number = 7, pokemon_species = species)
        val entry2 = PokemonEntry(entry_number = 7, pokemon_species = species)
        assertEquals(entry1, entry2)
    }

    @Test
    fun pokemonEntry_equality_differentEntryNumber_areNotEqual() {
        val species = PokemonSpecies(name = "squirtle", url = "https://pokeapi.co/api/v2/pokemon-species/7/")
        val entry1 = PokemonEntry(entry_number = 7, pokemon_species = species)
        val entry2 = PokemonEntry(entry_number = 8, pokemon_species = species)
        assertNotEquals(entry1, entry2)
    }

    @Test
    fun pokemonEntry_zeroEntryNumber() {
        val species = PokemonSpecies(name = "missingno", url = "")
        val entry = PokemonEntry(entry_number = 0, pokemon_species = species)
        assertEquals(0, entry.entry_number)
    }

    @Test
    fun pokemonEntry_negativeEntryNumber() {
        val species = PokemonSpecies(name = "glitch", url = "")
        val entry = PokemonEntry(entry_number = -1, pokemon_species = species)
        assertEquals(-1, entry.entry_number)
    }

    // ===== PokedexResponse Tests =====

    @Test
    fun pokedexResponse_creation_withEmptyList() {
        val response = PokedexResponse(pokemon_entries = emptyList())
        assertTrue(response.pokemon_entries.isEmpty())
    }

    @Test
    fun pokedexResponse_creation_withSingleEntry() {
        val species = PokemonSpecies(name = "bulbasaur", url = "https://pokeapi.co/api/v2/pokemon-species/1/")
        val entry = PokemonEntry(entry_number = 1, pokemon_species = species)
        val response = PokedexResponse(pokemon_entries = listOf(entry))
        assertEquals(1, response.pokemon_entries.size)
        assertEquals("bulbasaur", response.pokemon_entries[0].pokemon_species.name)
    }

    @Test
    fun pokedexResponse_creation_withMultipleEntries() {
        val entries = listOf(
            PokemonEntry(1, PokemonSpecies("bulbasaur", "url1")),
            PokemonEntry(4, PokemonSpecies("charmander", "url2")),
            PokemonEntry(7, PokemonSpecies("squirtle", "url3"))
        )
        val response = PokedexResponse(pokemon_entries = entries)
        assertEquals(3, response.pokemon_entries.size)
    }

    @Test
    fun pokedexResponse_entriesOrder_isPreserved() {
        val entries = listOf(
            PokemonEntry(7, PokemonSpecies("squirtle", "url3")),
            PokemonEntry(1, PokemonSpecies("bulbasaur", "url1")),
            PokemonEntry(4, PokemonSpecies("charmander", "url2"))
        )
        val response = PokedexResponse(pokemon_entries = entries)
        assertEquals("squirtle", response.pokemon_entries[0].pokemon_species.name)
        assertEquals("bulbasaur", response.pokemon_entries[1].pokemon_species.name)
        assertEquals("charmander", response.pokemon_entries[2].pokemon_species.name)
    }

    @Test
    fun pokedexResponse_findEntryByNumber() {
        val entries = listOf(
            PokemonEntry(1, PokemonSpecies("bulbasaur", "url1")),
            PokemonEntry(25, PokemonSpecies("pikachu", "url25")),
            PokemonEntry(150, PokemonSpecies("mewtwo", "url150"))
        )
        val response = PokedexResponse(pokemon_entries = entries)
        val pikachu = response.pokemon_entries.find { it.entry_number == 25 }
        assertNotNull(pikachu)
        assertEquals("pikachu", pikachu!!.pokemon_species.name)
    }

    @Test
    fun pokedexResponse_findEntryByNumber_notFound() {
        val entries = listOf(
            PokemonEntry(1, PokemonSpecies("bulbasaur", "url1"))
        )
        val response = PokedexResponse(pokemon_entries = entries)
        val notFound = response.pokemon_entries.find { it.entry_number == 999 }
        assertNull(notFound)
    }

    @Test
    fun pokedexResponse_filterByName() {
        val entries = listOf(
            PokemonEntry(1, PokemonSpecies("bulbasaur", "url1")),
            PokemonEntry(2, PokemonSpecies("ivysaur", "url2")),
            PokemonEntry(3, PokemonSpecies("venusaur", "url3")),
            PokemonEntry(4, PokemonSpecies("charmander", "url4"))
        )
        val response = PokedexResponse(pokemon_entries = entries)
        val saurs = response.pokemon_entries.filter { it.pokemon_species.name.contains("saur") }
        assertEquals(3, saurs.size)
    }
}
