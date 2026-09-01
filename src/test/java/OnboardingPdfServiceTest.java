public final class OnboardingPdfServiceTest {
    public static void main(String[] args) {
        assert "ACTIVE".equals(OnboardingPdfService.chooseState(true, true));
        assert "PENDING_REVIEW".equals(OnboardingPdfService.chooseState(true, false));
        System.out.println("Onboarding decision tests passed");
    }
}
