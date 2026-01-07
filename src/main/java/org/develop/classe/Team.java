package org.develop.classe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Team {

    private final Integer id;
    private final String name;
    private final ContinentEnum continent;
    private List<Player> players = new ArrayList<>();

    public Team(Integer id, String name, ContinentEnum continent) {
        this.id = id;
        this.name = name;
        this.continent = continent;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ContinentEnum getContinent() {
        return continent;
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public void addPlayer(Player player) {
        if (player != null && !players.contains(player)) {
            players.add(player);
        }
    }
    public Integer getPlayersGoals() {
        if (players == null || players.isEmpty()) {
            return 0;
        }

        int total = 0;
        for (Player player : players) {
            Integer goals = player.getGoalNb();
            if (goals == null) {
                throw new IllegalStateException(
                        "Impossible de calculer le total de buts de l'équipe '" +
                                name + "'. Le joueur '" + player.getName() +
                                "' a un nombre de buts inconnu."
                );
            }
            total += goals;
        }
        return total;
    }
    public Integer getPlayerCount() {
        return players.size();
    }

    public void setPlayers(List<Player> players) {
        this.players = (players != null) ? players : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Team team)) return false;
        return id == team.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Team{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", continent=" + continent +
                ", playerCount=" + players.size() +
                '}';
    }
}
