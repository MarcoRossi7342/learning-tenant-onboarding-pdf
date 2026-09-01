public final class OnboardingExample {
    public static void main(String[] args) throws Exception {
        String key = System.getenv("INFRAI_API_KEY");
        boolean approved = OnboardingPdfService.chooseState(true, true).equals("ACTIVE");
        OnboardingPdfService.Tenant tenant = new OnboardingPdfService.Tenant(
                "Northstar Learning", "admin@northstar.example", "course-team", approved ? "ACTIVE" : "PENDING_REVIEW");
        System.out.println("Account decision: " + tenant.state());
        if (key != null && !key.isBlank()) {
            String envelope = new OnboardingPdfService(key).createPacket(tenant);
            System.out.println("Generated onboarding PDF envelope: " + envelope);
        } else {
            System.out.println("Set INFRAI_API_KEY to generate the PDF with POST /v1/pdf/generate.");
        }
    }
}
