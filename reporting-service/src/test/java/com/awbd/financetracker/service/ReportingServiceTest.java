package com.awbd.financetracker.service;

import com.awbd.financetracker.client.FinanceCoreClient;
import com.awbd.financetracker.client.UserDirectoryClient;
import com.awbd.financetracker.enums.BillingFrequency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportingServiceTest {

    @Mock
    private UserDirectoryClient userDirectoryClient;

    @Mock
    private FinanceCoreClient financeCoreClient;

    private ReportingServiceImpl reportingService;

    @BeforeEach
    void setUp() {
        reportingService = new ReportingServiceImpl(userDirectoryClient, financeCoreClient);
    }

    @Test
    void calculateTotalMonthlySubscriptionCostNormalizesYearlyPricesAndSubtractsShares() {
        var streaming = subscription(1L, "Streaming", "24.00", BillingFrequency.MONTHLY, category("Entertainment"));
        var cloud = subscription(2L, "Cloud", "120.00", BillingFrequency.YEARLY, category("Tools"));
        var shared = share(streaming, "25.00", null);

        when(financeCoreClient.getSubscriptions(7L)).thenReturn(List.of(streaming, cloud));
        when(financeCoreClient.getSharesByOwner(7L)).thenReturn(List.of(shared));

        BigDecimal result = reportingService.calculateTotalMonthlySubscriptionCost(7L);

        assertThat(result).isEqualByComparingTo("28.00");
    }

    @Test
    void calculateDisposableIncomeSubtractsMonthlySubscriptionCostFromIncome() {
        when(userDirectoryClient.getUser(7L)).thenReturn(new UserDirectoryClient.UserSummary(
                7L,
                "Alex",
                "alex@example.com",
                new BigDecimal("1000.00")
        ));
        when(financeCoreClient.getSubscriptions(7L)).thenReturn(List.of(
                subscription(1L, "Streaming", "24.00", BillingFrequency.MONTHLY, category("Entertainment"))
        ));
        when(financeCoreClient.getSharesByOwner(7L)).thenReturn(List.of());

        BigDecimal result = reportingService.calculateDisposableIncome(7L);

        assertThat(result).isEqualByComparingTo("976.00");
    }

    @Test
    void getDashboardAggregatesIncomeCostsRenewalsAndCategorySpending() {
        var streaming = subscription(1L, "Streaming", "24.00", BillingFrequency.MONTHLY, category("Entertainment"));
        var cloud = subscription(2L, "Cloud", "120.00", BillingFrequency.YEARLY, category("Tools"));
        var upcoming = List.of(streaming);

        when(userDirectoryClient.getUser(7L)).thenReturn(new UserDirectoryClient.UserSummary(
                7L,
                "Alex",
                "alex@example.com",
                new BigDecimal("1000.00")
        ));
        when(financeCoreClient.getSubscriptions(7L)).thenReturn(List.of(streaming, cloud));
        when(financeCoreClient.getSharesByOwner(7L)).thenReturn(List.of());
        when(financeCoreClient.getUpcomingRenewals(7L)).thenReturn(upcoming);

        var dashboard = reportingService.getDashboard(7L);

        assertThat(dashboard.userId()).isEqualTo(7L);
        assertThat(dashboard.monthlyIncome()).isEqualByComparingTo("1000.00");
        assertThat(dashboard.totalMonthlySubscriptionCost()).isEqualByComparingTo("34.00");
        assertThat(dashboard.disposableIncome()).isEqualByComparingTo("966.00");
        assertThat(dashboard.totalAnnualSubscriptionCost()).isEqualByComparingTo("408.00");
        assertThat(dashboard.upcomingRenewals()).containsExactly(streaming);
        assertThat(dashboard.upcomingRenewalsCount()).isEqualTo(1);
        assertThat(dashboard.spendingByCategory()).containsEntry("Entertainment", new BigDecimal("24.00"));
        assertThat(dashboard.spendingByCategory()).containsEntry("Tools", new BigDecimal("10.00"));
    }

    @Test
    void getSpendingByCategorySubtractsSharedAwayAmountsWithoutGoingBelowZero() {
        var streaming = subscription(1L, "Streaming", "24.00", BillingFrequency.MONTHLY, category("Entertainment"));
        var shared = share(streaming, null, new BigDecimal("30.00"));

        when(financeCoreClient.getSubscriptions(7L)).thenReturn(List.of(streaming));
        when(financeCoreClient.getSharesByOwner(7L)).thenReturn(List.of(shared));

        Map<String, BigDecimal> result = reportingService.getSpendingByCategory(7L);

        assertThat(result).containsEntry("Entertainment", BigDecimal.ZERO);
    }

    private static FinanceCoreClient.CategoryDto category(String name) {
        return new FinanceCoreClient.CategoryDto(1L, name, null, 7L);
    }

    private static FinanceCoreClient.SubscriptionDto subscription(
            Long id,
            String name,
            String price,
            BillingFrequency billingFrequency,
            FinanceCoreClient.CategoryDto category
    ) {
        return new FinanceCoreClient.SubscriptionDto(
                id,
                name,
                new BigDecimal(price),
                billingFrequency,
                LocalDate.now().plusDays(5),
                7L,
                category,
                null
        );
    }

    private static FinanceCoreClient.SubscriptionShareDto share(
            FinanceCoreClient.SubscriptionDto subscription,
            String percentageShare,
            BigDecimal fixedAmount
    ) {
        return new FinanceCoreClient.SubscriptionShareDto(
                subscription.id(),
                8L,
                percentageShare == null ? null : new BigDecimal(percentageShare),
                fixedAmount,
                null,
                subscription
        );
    }
}
