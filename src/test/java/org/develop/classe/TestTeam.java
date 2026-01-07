package org.develop.classe;

public class TestTeam {
    public static void main(String[] args) {
        // Utilise n'importe quelle valeur de ContinentEnum
        // Par exemple, la première disponible
        ContinentEnum continent = ContinentEnum.values()[0];
        Team team = new Team(1, "Test Team", continent);

        // Ajouter quelques joueurs
        team.addPlayer(new Player(1, "Joueur A", 25, PlayerPositionEnum.MIDF, team, 3));
        team.addPlayer(new Player(2, "Joueur B", 30, PlayerPositionEnum.STR, team, 4));

        System.out.println("Total buts (devrait être 7): " + team.getPlayersGoals());

        // Ajouter un joueur avec buts inconnus
        team.addPlayer(new Player(3, "Joueur C", 28, PlayerPositionEnum.DEF, team, null));

        try {
            team.getPlayersGoals();
            System.out.println("ERREUR: l'exception aurait dû être levée !");
        } catch (IllegalStateException e) {
            System.out.println("SUCCÈS: Exception levée comme attendu: " + e.getMessage());
        }
    }
}