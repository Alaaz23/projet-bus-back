package bustrack.example.bustrack.services;

import bustrack.example.bustrack.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class StatisticsService {

    @Autowired private SalarieRepository salarieRepository;
    @Autowired private BusRepository busRepository;
    @Autowired private FeedbackRepository feedbackRepository;
    @Autowired private TragetRepository tragetRepository;

    public Map<String, Object> getGlobalStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        long totalSalaries = salarieRepository.count();
        long totalBus = busRepository.count();
        long totalFeedbacks = feedbackRepository.count();
        long totalTrajets = tragetRepository.count();
        long feedbacksNonLus = feedbackRepository.countByChecked(false);

        stats.put("totalSalaries", totalSalaries);
        stats.put("totalBus", totalBus);
        stats.put("totalFeedbacks", totalFeedbacks);
        stats.put("totalTrajets", totalTrajets);
        stats.put("feedbacksNonLus", feedbacksNonLus);

        // Répartition bus par statut
        Map<String, Long> busParStatut = new LinkedHashMap<>();
        busParStatut.put("EN_ROUTE", busRepository.countByStatut("EN_ROUTE"));
        busParStatut.put("A_LARRET", busRepository.countByStatut("A_LARRET"));
        busParStatut.put("HORS_SERVICE", busRepository.countByStatut("HORS_SERVICE"));
        stats.put("busParStatut", busParStatut);

        // Feedbacks lus vs non lus
        Map<String, Long> feedbackStatus = new LinkedHashMap<>();
        feedbackStatus.put("lus", feedbackRepository.countByChecked(true));
        feedbackStatus.put("nonLus", feedbackRepository.countByChecked(false));
        stats.put("feedbackStatus", feedbackStatus);

        // Données 7 derniers jours simulées (à remplacer par vraies données si TrajetHistory est implémenté)
        List<Map<String, Object>> trajetsSemaine = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            Map<String, Object> day = new LinkedHashMap<>();
            day.put("date", date.format(fmt));
            // Valeur simulée basée sur le jour de semaine
            int dayOfWeek = date.getDayOfWeek().getValue();
            long count = (dayOfWeek <= 5) ? totalBus : totalBus / 2; // jours ouvrés vs week-end
            day.put("trajets", count);
            trajetsSemaine.add(day);
        }
        stats.put("trajetsSemaine", trajetsSemaine);

        // Taux remplissage moyen (basé sur capacite des bus)
        double tauxRemplissage = busRepository.findAll().stream()
                .mapToInt(b -> b.getCapacite())
                .average()
                .orElse(0);
        // Simuler taux = (salaries / (capacite * nombre de bus)) * 100
        double taux = (totalBus > 0 && tauxRemplissage > 0)
                ? Math.min(100, (totalSalaries * 100.0) / (tauxRemplissage * totalBus))
                : 0;
        stats.put("tauxRemplissage", Math.round(taux));

        return stats;
    }
}
