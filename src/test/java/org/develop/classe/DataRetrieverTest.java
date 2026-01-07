package org.develop.classe;

import org.junit.jupiter.api.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DataRetrieverTest {

    private DataRetriever dataRetriever;

    @BeforeEach
    void setUp() {
        dataRetriever = new DataRetriever();
    }

    /* a) */
    @Test
    void findTeamById_realMadrid() {
        Team team = dataRetriever.findTeamById(1);
        assertNotNull(team);
        assertEquals("Real Madrid CF", team.getName());
        assertEquals(3, team.getPlayers().size());
    }

    /* b) */
    @Test
    void findTeamById_interMiami_noPlayers() {
        Team team = dataRetriever.findTeamById(5);
        assertNotNull(team);
        assertEquals("Inter Miami", team.getName());
        assertTrue(team.getPlayers().isEmpty());
    }

    /* c) */
    @Test
    void findPlayers_page1_size2() {
        List<Player> players = dataRetriever.findPlayers(1, 2);
        assertEquals(2, players.size());
        assertEquals("Thibaut Courtois", players.get(0).getName());
        assertEquals("Dani Carvajal", players.get(1).getName());
    }

    /* d) */
    @Test
    void findPlayers_page3_size5_empty() {
        List<Player> players = dataRetriever.findPlayers(3, 5);
        assertTrue(players.isEmpty());
    }

    /* e) */
    @Test
    void findTeamsByPlayerName_an() {
        List<Team> teams = dataRetriever.findTeamsByPlayerName("an");
        assertEquals(2, teams.size());
        assertTrue(teams.stream().anyMatch(t -> t.getName().contains("Real")));
        assertTrue(teams.stream().anyMatch(t -> t.getName().contains("Atletico")));
    }

    /* f) */
    @Test
    void findPlayersByCriteria_ud_midf_madrid() {
        List<Player> players = dataRetriever.findPlayersByCriteria(
                "ud", PlayerPositionEnum.MIDF, "Madrid",
                ContinentEnum.EUROPA, 1, 10
        );
        assertEquals(1, players.size());
        assertEquals("Jude Bellingham", players.get(0).getName());
    }

    /* g) */
    @Test
    void createPlayers_runtimeException() {
        List<Player> players = List.of(
                new Player(6, "Jude Bellingham", 23, PlayerPositionEnum.STR, null),
                new Player(7, "Pedri", 24, PlayerPositionEnum.MIDF, null)
        );
        assertThrows(RuntimeException.class,
                () -> dataRetriever.createPlayers(players));
    }

    /* h) */
    @Test
    void createPlayers_success() {
        List<Player> players = List.of(
                new Player(6, "Vini", 25, PlayerPositionEnum.STR, null),
                new Player(7, "Pedri", 24, PlayerPositionEnum.MIDF, null)
        );
        List<Player> res = dataRetriever.createPlayers(players);
        assertEquals(2, res.size());
    }

    /* i) */
    @Test
    void saveTeam_addPlayer() {
        Team team = dataRetriever.findTeamById(1);
        team.addPlayer(new Player(6, "Vini", 25, PlayerPositionEnum.STR, null));
        Team updated = dataRetriever.saveTeam(team);
        assertNotNull(updated);
    }

    /* j) */
    @Test
    void saveTeam_removeAllPlayers() {
        Team team = dataRetriever.findTeamById(2);
        team.getPlayers().clear();
        Team updated = dataRetriever.saveTeam(team);
        assertTrue(updated.getPlayers().isEmpty());
    }
}
