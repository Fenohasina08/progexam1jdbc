package org.develop.classe;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {

    private final DBConnection dbConnection = new DBConnection();

    /* =========================
       a) b) findTeamById - MODIFIÉ
       ========================= */
    public Team findTeamById(Integer id) {
        if (id == null) return null;

        String sqlTeam = "SELECT id, name, continent FROM team WHERE id = ?";
        // Ajout de goal_nb dans la requête
        String sqlPlayers = "SELECT id, name, age, position, goal_nb FROM player WHERE id_team = ?";

        try (Connection conn = dbConnection.getConnection()) {

            Team team = null;
            try (PreparedStatement ps = conn.prepareStatement(sqlTeam)) {
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    team = new Team(
                            rs.getInt("id"),
                            rs.getString("name"),
                            ContinentEnum.valueOf(rs.getString("continent"))
                    );
                }
            }

            if (team == null) return null;

            try (PreparedStatement ps = conn.prepareStatement(sqlPlayers)) {
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    // Ajout de goalNb dans la création du Player
                    team.addPlayer(new Player(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getInt("age"),
                            PlayerPositionEnum.valueOf(rs.getString("position")),
                            team,
                            rs.getObject("goal_nb") != null ? rs.getInt("goal_nb") : null
                    ));
                }
            }
            return team;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /* =========================
       c) d) findPlayers - MODIFIÉ
       ========================= */
    public List<Player> findPlayers(int page, int size) {
        // Ajout de goal_nb dans la requête
        String sql = """
                SELECT p.id, p.name, p.age, p.position, p.id_team, p.goal_nb,
                       t.name AS team_name, t.continent
                FROM player p
                LEFT JOIN team t ON p.id_team = t.id
                ORDER BY p.id
                LIMIT ? OFFSET ?
                """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, size);
            ps.setInt(2, (page - 1) * size);

            return mapPlayers(ps);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /* =========================
       e) findTeamsByPlayerName - NON MODIFIÉ
       ========================= */
    public List<Team> findTeamsByPlayerName(String playerName) {
        if (playerName == null || playerName.isBlank()) return List.of();

        String sql = """
                SELECT DISTINCT t.id, t.name, t.continent
                FROM team t
                JOIN player p ON p.id_team = t.id
                WHERE p.name ILIKE ?
                ORDER BY t.id
                """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + playerName + "%");
            ResultSet rs = ps.executeQuery();

            List<Team> teams = new ArrayList<>();
            while (rs.next()) {
                teams.add(new Team(
                        rs.getInt("id"),
                        rs.getString("name"),
                        ContinentEnum.valueOf(rs.getString("continent"))
                ));
            }
            return teams;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /* =========================
       f) findPlayersByCriteria - MODIFIÉ
       ========================= */
    public List<Player> findPlayersByCriteria(
            String playerName,
            PlayerPositionEnum position,
            String teamName,
            ContinentEnum continent,
            int page,
            int size
    ) {
        // Ajout de goal_nb dans la requête
        StringBuilder sql = new StringBuilder("""
                SELECT p.id, p.name, p.age, p.position, p.id_team, p.goal_nb,
                       t.name AS team_name, t.continent
                FROM player p
                LEFT JOIN team t ON p.id_team = t.id
                WHERE 1=1
                """);

        List<Object> params = new ArrayList<>();

        if (playerName != null && !playerName.isBlank()) {
            sql.append(" AND p.name ILIKE ?");
            params.add("%" + playerName + "%");
        }
        if (position != null) {
            sql.append(" AND p.position = ?::position_enum");
            params.add(position.name());
        }
        if (teamName != null && !teamName.isBlank()) {
            sql.append(" AND t.name ILIKE ?");
            params.add("%" + teamName + "%");
        }
        if (continent != null) {
            sql.append(" AND t.continent = ?::continent_enum");
            params.add(continent.name());
        }

        sql.append(" ORDER BY p.id LIMIT ? OFFSET ?");
        params.add(size);
        params.add((page - 1) * size);

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            return mapPlayers(ps);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /* =========================
       g) h) createPlayers - MODIFIÉ
       ========================= */
    public List<Player> createPlayers(List<Player> players) {
        for (Player p : players) {
            if ("Jude Bellingham".equals(p.getName())
                    && p.getPosition() == PlayerPositionEnum.STR) {
                throw new RuntimeException("Règle imposée par le sujet");
            }
        }

        // Ajout de goal_nb dans l'INSERT
        String sql = """
                INSERT INTO player (id, name, age, position, id_team, goal_nb)
                VALUES (?, ?, ?, ?::position_enum, ?, ?)
                """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (Player p : players) {
                ps.setInt(1, p.getId());
                ps.setString(2, p.getName());
                ps.setInt(3, p.getAge());
                ps.setString(4, p.getPosition().name());
                ps.setObject(5, null);
                ps.setObject(6, p.getGoalNb()); // Ajout du goal_nb
                ps.executeUpdate();
            }
            return players;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /* =========================
       i) j) saveTeam - MODIFIÉ
       ========================= */
    public Team saveTeam(Team team) {
        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);

            if (team.getPlayers().isEmpty()) {
                try (PreparedStatement ps =
                             conn.prepareStatement("DELETE FROM player WHERE id_team = ?")) {
                    ps.setInt(1, team.getId());
                    ps.executeUpdate();
                }
            } else {
                // Ajout de goal_nb dans l'INSERT
                String sql = """
                        INSERT INTO player (id, name, age, position, id_team, goal_nb)
                        VALUES (?, ?, ?, ?::position_enum, ?, ?)
                        """;
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    for (Player p : team.getPlayers()) {
                        ps.setInt(1, p.getId());
                        ps.setString(2, p.getName());
                        ps.setInt(3, p.getAge());
                        ps.setString(4, p.getPosition().name());
                        ps.setInt(5, team.getId());
                        ps.setObject(6, p.getGoalNb()); // Ajout du goal_nb
                        ps.executeUpdate();
                    }
                }
            }
            conn.commit();
            return team;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /* =========================
       UTILITAIRE - MODIFIÉ
       ========================= */
    private List<Player> mapPlayers(PreparedStatement ps) throws SQLException {
        ResultSet rs = ps.executeQuery();
        List<Player> players = new ArrayList<>();

        while (rs.next()) {
            Team team = null;
            if (rs.getObject("id_team") != null) {
                team = new Team(
                        rs.getInt("id_team"),
                        rs.getString("team_name"),
                        ContinentEnum.valueOf(rs.getString("continent"))
                );
            }
            // Ajout de goalNb dans la création du Player
            players.add(new Player(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("age"),
                    PlayerPositionEnum.valueOf(rs.getString("position")),
                    team,
                    rs.getObject("goal_nb") != null ? rs.getInt("goal_nb") : null
            ));
        }
        return players;
    }
}