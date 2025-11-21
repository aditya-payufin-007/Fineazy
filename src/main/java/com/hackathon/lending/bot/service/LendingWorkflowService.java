package com.hackathon.lending.bot.service;

import com.hackathon.lending.bot.entity.UserDetails;
import com.hackathon.lending.bot.repository.UserDetailsRepository;
import com.hackathon.lending.bot.utility.ApplicationStages;
import com.hackathon.lending.bot.utility.ApplicationStages.StageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class LendingWorkflowService {
    
    private static final Logger logger = LoggerFactory.getLogger(LendingWorkflowService.class);
    
    @Autowired
    private UserDetailsRepository userDetailsRepository;
    
    /**
     * Process user input based on current stage
     */
    public String processUserInput(String mobileId, String userInput, String currentStageValue) {
        ApplicationStages currentStage = ApplicationStages.fromValue(currentStageValue);
        logger.info("Processing input for stage: {}", currentStage.name());
        
        StageType stageType = currentStage.getStageType();
        switch (stageType) {
            case ONBOARDING:
                return handleOnboarding(mobileId, userInput);
            case APPLICATION_CREATION:
                return handleCreateApplication(mobileId, userInput);
            case APPLICATION_UPDATE:
                return handleApplicationUpdate(mobileId, userInput);
            case KYC:
                return handleKYC(mobileId, userInput);
            case ELIGIBILITY:
                return handleEligibility(mobileId, userInput);
            case OFFER:
            case OFFER_ACCEPTANCE:
                return handleOffer(mobileId, userInput);
            case DOCUMENTS_VERIFICATION:
                return handleDocumentsVerification(mobileId, userInput);
            case DOCUMENT_SIGNING:
                return handleDocumentSigning(mobileId, userInput);
            case DISBURSAL:
                return handleDisbursal(mobileId, userInput);
            case POST_DISBURSAL:
                return handlePostDisbursal(mobileId, userInput);
            default:
                return "Invalid stage. Please contact support.";
        }
    }
    
    /**
     * Handle onboarding stage
     */
    private String handleOnboarding(String mobileId, String userInput) {
        String response = "Welcome to our lending service! 🏦\n\n" +
                "We help you get loans quickly and easily.\n\n" +
                "To get started, please reply with your full name.";
        
        // If user provided a name, move to next stage
        if (userInput != null && !userInput.trim().isEmpty() && userInput.length() > 2) {
            UserDetails userDetails = userDetailsRepository.findById(mobileId)
                    .orElseGet(() -> {
                        UserDetails newUser = new UserDetails();
                        newUser.setMobileId(mobileId);
                        newUser.setCurrentStage(ApplicationStages.defaultStage().name());
                        return newUser;
                    });
            userDetails.setName(userInput.trim());
            userDetailsRepository.save(userDetails);
            
            updateStage(mobileId, ApplicationStages.APPLICATION_CREATION_IN_PROGRESS);
            
            response = String.format("Thank you, %s! ✅\n\n" +
                    "Now let's create your loan application.\n\n" +
                    "Would you like to:\n" +
                    "1. Apply for a Personal Loan\n" +
                    "2. Apply for a Business Loan\n\n" +
                    "Please reply with 1 or 2.", userInput.trim());
        }
        
        return response;
    }
    
    /**
     * Handle create application stage
     */
    private String handleCreateApplication(String mobileId, String userInput) {
        String applicationId = "APP" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        Optional<UserDetails> userDetailsOpt = userDetailsRepository.findById(mobileId);
        if (userDetailsOpt.isPresent()) {
            UserDetails userDetails = userDetailsOpt.get();
            userDetails.setApplicationId(applicationId);
            userDetailsRepository.save(userDetails);
        }
        
        updateStage(mobileId, ApplicationStages.KYC_IN_PROGRESS);
        
        return String.format("Great! Your application ID is: %s\n\n" +
                "Now let's complete your KYC (Know Your Customer) process.\n\n" +
                "Please provide your Aadhaar number (12 digits):", applicationId);
    }
    
    /**
     * Handle KYC stage
     */
    private String handleKYC(String mobileId, String userInput) {
        Optional<UserDetails> userDetailsOpt = userDetailsRepository.findById(mobileId);
        
        if (!userDetailsOpt.isPresent()) {
            return "User details not found. Please start from the beginning.";
        }
        
        UserDetails userDetails = userDetailsOpt.get();
        
        // Check if Aadhaar is already provided
        if (userDetails.getAadhaar() != null && !userDetails.getAadhaar().isEmpty()) {
            // Aadhaar already provided, move to eligibility
            updateStage(mobileId, ApplicationStages.ELIGIBILITY_IN_PROGRESS);
            return "✅ **Thank you! Your Aadhaar details have been securely saved.**\n\n" +
                    "📋 **KYC Information Updated Successfully**\n\n" +
                    "---\n\n" +
                    "**Next Step: Eligibility Assessment**\n\n" +
                    "To proceed with checking your loan eligibility, we require your consent to:\n\n" +
                    "• Access your credit bureau report (CIBIL/Experian/Equifax)\n" +
                    "• Verify your financial profile and credit history\n" +
                    "• Assess your loan eligibility based on our lending criteria\n\n" +
                    "**Your data is secure and will be used solely for loan processing purposes.**\n\n" +
                    "💬 **Please reply with 'YES, I CONSENT' or 'PROCEED' to authorize the eligibility check.**\n\n" +
                    "---\n" +
                    "*By proceeding, you agree to our credit assessment process and data usage for loan evaluation.*";
        }
        
        // Validate and save Aadhaar
        String aadhaarInput = userInput.trim();
        
        // Check if input is empty
        if (aadhaarInput.isEmpty()) {
            return "❌ Aadhaar number cannot be empty.\n\n" +
                    "Please provide your Aadhaar number (12 digits):";
        }
        
        // Remove spaces and hyphens if any
        aadhaarInput = aadhaarInput.replaceAll("[\\s-]", "");
        
        // Validate Aadhaar format: must be exactly 12 digits
        if (!aadhaarInput.matches("^\\d{12}$")) {
            return "❌ Invalid Aadhaar number format.\n\n" +
                    "Aadhaar number must be exactly 12 digits (numbers only).\n\n" +
                    "Please provide your Aadhaar number again:";
        }
        
        // Save Aadhaar
        userDetails.setAadhaar(aadhaarInput);
        userDetailsRepository.save(userDetails);
        
        // Move to eligibility stage
        updateStage(mobileId, ApplicationStages.ELIGIBILITY_IN_PROGRESS);
        
        return "✅ **Thank you! Your Aadhaar details have been securely saved.**\n\n" +
       "📋 **KYC Information Updated Successfully**\n\n" +
       "---\n\n" +
       "**Next Step: Eligibility Assessment**\n\n" +
       "To proceed with checking your loan eligibility, we require your consent to:\n\n" +
       "• Access your credit bureau report (CIBIL/Experian/Equifax)\n" +
       "• Verify your financial profile and credit history\n" +
       "• Assess your loan eligibility based on our lending criteria\n\n" +
       "**Your data is secure and will be used solely for loan processing purposes.**\n\n" +
       "💬 **Please reply with 'YES, I CONSENT' or 'PROCEED' to authorize the eligibility check.**\n\n" +
       "---\n" +
       "*By proceeding, you agree to our credit assessment process and data usage for loan evaluation.*";

    }
    
    /**
     * Handle eligibility stage
     */
    private String handleEligibility(String mobileId, String userInput) {
        // Check if user has provided consent
        String trimmedInput = userInput != null ? userInput.trim() : "";
        
        // If input is empty, ask for consent
        if (trimmedInput.isEmpty()) {
            return "❌ **No response received.**\n\n" +
                    "To proceed with checking your loan eligibility, we require your consent.\n\n" +
                    "💬 **Please reply with 'YES, I CONSENT' or 'PROCEED' to authorize the eligibility check.**";
        }
        
        // Check for valid consent responses (case-insensitive)
        String upperInput = trimmedInput.toUpperCase();
        boolean hasConsent = upperInput.equals("YES, I CONSENT") || 
                            upperInput.equals("YES I CONSENT") ||
                            upperInput.equals("PROCEED") ||
                            upperInput.equals("YES") ||
                            upperInput.startsWith("YES,") ||
                            upperInput.contains("CONSENT");
        
        // If user hasn't provided proper consent, show error message
        if (!hasConsent) {
            return "❌ **Invalid response. Consent required to proceed.**\n\n" +
                    "To check your loan eligibility, we need your explicit consent.\n\n" +
                    "**Please reply with one of the following:**\n" +
                    "• 'YES, I CONSENT'\n" +
                    "• 'PROCEED'\n\n" +
                    "**Your consent is required to:**\n" +
                    "• Access your credit bureau report\n" +
                    "• Verify your financial profile\n" +
                    "• Assess your loan eligibility\n\n" +
                    "💬 Please provide your consent to continue.";
        }
        
        // User has provided consent, perform eligibility check
        // Mock eligibility check - in real scenario, this would call an external service
        updateStage(mobileId, ApplicationStages.OFFER_ACCEPTANCE_IN_PROGRESS);
        
        return "✅ **Great news! You are eligible for a loan!** 🎉\n\n" +
                "**Loan Offer Details:**\n\n" +
                "💰 **Loan Amount:** ₹5,00,000\n" +
                "📊 **Interest Rate:** 12% per annum\n" +
                "⏰ **Tenure:** Up to 36 months\n\n" +
                "---\n\n" +
                "**Would you like to accept this loan offer?**\n\n" +
                "If you accept, we will proceed to the document signing process.\n\n" +
                "💬 **Please reply with 'YES' to accept or 'NO' to decline.**";
    }
    
    /**
     * Handle offer stage
     */
    private String handleOffer(String mobileId, String userInput) {
        // Check if input is empty
        if (userInput == null || userInput.trim().isEmpty()) {
            return "❌ **No response received.**\n\n" +
                    "To proceed with your loan application, please provide your decision.\n\n" +
                    "💬 **Please reply with 'YES' to accept the offer or 'NO' to decline.**";
        }
        
        String trimmedInput = userInput.trim();
        String upperInput = trimmedInput.toUpperCase();
        
        // Check for acceptance responses (case-insensitive)
        boolean isAccepted = upperInput.equals("YES") ||
                            upperInput.equals("Y") ||
                            upperInput.equals("OK") ||
                            upperInput.equals("OKAY") ||
                            upperInput.equals("PROCEED") ||
                            upperInput.equals("ACCEPT") ||
                            upperInput.equals("AGREE") ||
                            upperInput.startsWith("YES,") ||
                            upperInput.startsWith("YES ");
        
        // Check for rejection responses (case-insensitive)
        boolean isDeclined = upperInput.equals("NO") ||
                            upperInput.equals("N") ||
                            upperInput.equals("DECLINE") ||
                            upperInput.equals("REJECT") ||
                            upperInput.equals("CANCEL") ||
                            upperInput.startsWith("NO,") ||
                            upperInput.startsWith("NO ");
        
        if (isAccepted) {
            // User accepted the offer, move to document signing
            updateStage(mobileId, ApplicationStages.DOCUMENT_SIGNING_IN_PROGRESS);
            
            return "✅ **Thank you for accepting the loan offer!**\n\n" +
                    "We're now proceeding to the document signing process.\n\n" +
                    "**Next Steps:**\n" +
                    "You will receive the loan agreement documents for your review and signature.\n\n" +
                    "💬 **Once you have reviewed and signed the documents, please reply with 'SIGNED' to confirm.**\n\n" +
                    "---\n" +
                    "*Our team will process your application upon receipt of signed documents.*";
        } else if (isDeclined) {
            // User declined the offer
            updateStage(mobileId, ApplicationStages.OFFER_DECLINED);
            return "**Thank you for your response.**\n\n" +
                    "We understand that you have chosen not to proceed with this loan offer at this time.\n\n" +
                    "If you change your mind or have any questions, please feel free to reach out to us.\n\n" +
                    "Is there anything else we can help you with today?";
        }
        
        // Invalid response - show error with instructions
        return "❌ **Invalid response. Please provide a clear decision.**\n\n" +
                "To proceed with your loan application, we need your decision on the offer.\n\n" +
                "**Please reply with one of the following:**\n" +
                "• 'YES' or 'Y' - to accept the loan offer\n" +
                "• 'NO' or 'N' - to decline the loan offer\n\n" +
                "💬 **Your response will help us proceed with the next steps in your loan application.**";
    }
    
    /**
     * Handle documents verification stage
     */
    private String handleDocumentsVerification(String mobileId, String userInput) {
        if (userInput.trim().equalsIgnoreCase("DONE")) {
            updateStage(mobileId, ApplicationStages.DISBURSAL_IN_PROGRESS);
            
            return "Documents received! 📄✅\n\n" +
                    "Our team will verify your documents within 24 hours.\n" +
                    "Once verified, we'll proceed with the disbursal.\n\n" +
                    "You'll receive a notification once the amount is disbursed to your account.";
        }
        
        return "Please upload all required documents and reply 'DONE' when ready.";
    }
    
    /**
     * Handle disbursal stage
     */
    private String handleDisbursal(String mobileId, String userInput) {
        // Check if input is empty
        if (userInput == null || userInput.trim().isEmpty()) {
            return "❌ **No response received.**\n\n" +
                    "To complete the loan disbursal process, please provide your confirmation.\n\n" +
                    "💬 **Please reply with 'CONFIRM' or 'OK' to acknowledge and complete the disbursal process.**";
        }
        
        String trimmedInput = userInput.trim();
        String upperInput = trimmedInput.toUpperCase();
        
        // Check for confirmation responses (case-insensitive)
        boolean isConfirmed = upperInput.equals("CONFIRM") ||
                             upperInput.equals("OK") ||
                             upperInput.equals("OKAY") ||
                             upperInput.equals("YES") ||
                             upperInput.equals("Y") ||
                             upperInput.equals("PROCEED") ||
                             upperInput.equals("ACCEPT") ||
                             upperInput.equals("AGREE") ||
                             upperInput.equals("DONE") ||
                             upperInput.equals("COMPLETE") ||
                             upperInput.startsWith("CONFIRM,") ||
                             upperInput.startsWith("CONFIRM ") ||
                             upperInput.contains("CONFIRM");
        
        if (isConfirmed) {
            // User confirmed, complete disbursal and move to post-disbursal
            updateStage(mobileId, ApplicationStages.POST_DISBURSAL_IN_PROGRESS);
            
            return "🎉 **Congratulations! Your loan has been successfully disbursed!** 🎉\n\n" +
                    "**Loan Disbursal Completed**\n\n" +
                    "✅ **Loan Amount:** ₹5,00,000\n" +
                    "📧 **Notification:** You will receive SMS and Email confirmation shortly\n" +
                    "⏰ **Credit Timeline:** The amount will reflect in your account within 2-3 business hours\n\n" +
                    "---\n\n" +
                    "**What's Next?**\n\n" +
                    "• Track your loan status anytime by replying with 'STATUS'\n" +
                    "• View repayment schedule and EMI details\n" +
                    "• Make payments and manage your loan account\n\n" +
                    "**Loan Details:**\n" +
                    "• Loan Amount: ₹5,00,000\n" +
                    "• Interest Rate: 12% per annum\n" +
                    "• Tenure: 36 months\n" +
                    "• EMI Amount: ₹15,000 (approx.)\n\n" +
                    "💬 **Reply with 'STATUS' to check your loan details and repayment schedule.**\n\n" +
                    "---\n" +
                    "*Thank you for choosing our lending services. We're here to help!*";
        }
        
        // Invalid response - show error with instructions
        return "❌ **Invalid response. Please confirm to complete the disbursal process.**\n\n" +
                "To finalize your loan disbursal, we need your confirmation.\n\n" +
                "**Please reply with one of the following:**\n" +
                "• 'CONFIRM' - to confirm and complete the disbursal\n" +
                "• 'OK' or 'YES' - to proceed with disbursal\n" +
                "• 'DONE' or 'COMPLETE' - to acknowledge completion\n\n" +
                "💬 **Once you confirm, your loan amount will be disbursed to your registered account.**";
    }
    
    /**
     * Handle post disbursal stage
     */
    private String handlePostDisbursal(String mobileId, String userInput) {
        // Check if input is empty
        if (userInput == null || userInput.trim().isEmpty()) {
            return getPostDisbursalWelcomeMessage();
        }
        
        String trimmedInput = userInput.trim();
        String upperInput = trimmedInput.toUpperCase();
        
        // Handle STATUS command
        if (upperInput.equals("STATUS") || upperInput.equals("LOAN STATUS") || upperInput.equals("BALANCE")) {
            return getLoanStatusMessage();
        }
        
        // Handle REPAYMENT/SCHEDULE command
        if (upperInput.equals("REPAYMENT") || upperInput.equals("SCHEDULE") || 
            upperInput.equals("EMI") || upperInput.equals("REPAYMENT SCHEDULE") ||
            upperInput.equals("PAYMENT SCHEDULE")) {
            return getRepaymentScheduleMessage();
        }
        
        // Handle PAY/PAYMENT command
        if (upperInput.equals("PAY") || upperInput.equals("PAYMENT") || 
            upperInput.equals("MAKE PAYMENT") || upperInput.startsWith("PAY ")) {
            return getPaymentInstructionsMessage();
        }
        
        // Handle HELP command
        if (upperInput.equals("HELP") || upperInput.equals("SUPPORT") || 
            upperInput.equals("ASSISTANCE") || upperInput.equals("INFO")) {
            return getHelpMessage();
        }
        
        // Handle NEXT EMI command
        if (upperInput.equals("NEXT EMI") || upperInput.equals("NEXT PAYMENT") ||
            upperInput.equals("UPCOMING EMI")) {
            return getNextEMIMessage();
        }
        
        // Handle ACCOUNT/LOAN DETAILS command
        if (upperInput.equals("ACCOUNT") || upperInput.equals("DETAILS") ||
            upperInput.equals("LOAN DETAILS") || upperInput.equals("ACCOUNT DETAILS")) {
            return getLoanDetailsMessage();
        }
        
        // Default response for unrecognized commands
        return getPostDisbursalWelcomeMessage() + "\n\n" +
                "❌ **Command not recognized.**\n\n" +
                "Please use one of the available commands listed above.";
    }
    
    /**
     * Get welcome message for post-disbursal stage
     */
    private String getPostDisbursalWelcomeMessage() {
        return "🏦 **Welcome to Your Loan Account**\n\n" +
                "How can I assist you today?\n\n" +
                "**Available Commands:**\n" +
                "• **STATUS** - Check your loan balance and status\n" +
                "• **REPAYMENT** - View your repayment schedule\n" +
                "• **PAY** - Make a payment\n" +
                "• **NEXT EMI** - View next EMI details\n" +
                "• **ACCOUNT** - View complete loan account details\n" +
                "• **HELP** - Get assistance and support\n\n" +
                "💬 **Simply type any command to get started.**";
    }
    
    /**
     * Get loan status message
     */
    private String getLoanStatusMessage() {
        return "📊 **Your Loan Status**\n\n" +
                "**Loan Summary:**\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "💰 **Loan Amount:** ₹5,00,000\n" +
                "📉 **Outstanding Principal:** ₹4,85,000\n" +
                "💵 **Total Outstanding:** ₹4,90,000\n" +
                "✅ **Amount Paid:** ₹15,000\n" +
                "📅 **Loan Start Date:** 01-Nov-2024\n" +
                "📅 **Loan End Date:** 01-Nov-2027\n\n" +
                "**Next Payment:**\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "💳 **Next EMI:** ₹15,000\n" +
                "📅 **Due Date:** 05-Dec-2024\n" +
                "⏰ **Days Remaining:** 12 days\n" +
                "📊 **Status:** Upcoming\n\n" +
                "**Quick Actions:**\n" +
                "• Type **PAY** to make a payment\n" +
                "• Type **REPAYMENT** to view full schedule\n" +
                "• Type **NEXT EMI** for detailed EMI information\n\n" +
                "💬 **Need help? Type HELP for assistance.**";
    }
    
    /**
     * Get repayment schedule message
     */
    private String getRepaymentScheduleMessage() {
        return "📅 **Your Repayment Schedule**\n\n" +
                "**Loan Details:**\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "💰 **Loan Amount:** ₹5,00,000\n" +
                "📊 **Interest Rate:** 12% per annum\n" +
                "⏰ **Tenure:** 36 months\n" +
                "💳 **EMI Amount:** ₹15,000 (approx.)\n\n" +
                "**Upcoming EMIs (Next 6 Months):**\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "1️⃣ **Dec 2024** - ₹15,000 (Due: 05-Dec-2024)\n" +
                "2️⃣ **Jan 2025** - ₹15,000 (Due: 05-Jan-2025)\n" +
                "3️⃣ **Feb 2025** - ₹15,000 (Due: 05-Feb-2025)\n" +
                "4️⃣ **Mar 2025** - ₹15,000 (Due: 05-Mar-2025)\n" +
                "5️⃣ **Apr 2025** - ₹15,000 (Due: 05-Apr-2025)\n" +
                "6️⃣ **May 2025** - ₹15,000 (Due: 05-May-2025)\n\n" +
                "**Payment Summary:**\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "✅ **EMIs Paid:** 1 of 36\n" +
                "📊 **Remaining EMIs:** 35\n" +
                "💵 **Total Amount Paid:** ₹15,000\n" +
                "📉 **Outstanding Balance:** ₹4,85,000\n\n" +
                "**Quick Actions:**\n" +
                "• Type **PAY** to make your next payment\n" +
                "• Type **STATUS** to check current loan status\n" +
                "• Type **NEXT EMI** for detailed next payment info\n\n" +
                "💬 **For complete schedule, please contact our support team.**";
    }
    
    /**
     * Get payment instructions message
     */
    private String getPaymentInstructionsMessage() {
        return "💳 **Make a Payment**\n\n" +
                "**Payment Options:**\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                "**1. UPI Payment:**\n" +
                "• Use UPI ID: loan@bankname\n" +
                "• Amount: ₹15,000 (or any amount)\n" +
                "• Reference: Your Application ID\n\n" +
                "**2. NEFT/RTGS:**\n" +
                "• Bank Name: [Bank Name]\n" +
                "• Account Number: [Account Number]\n" +
                "• IFSC Code: [IFSC Code]\n" +
                "• Account Name: [Account Name]\n\n" +
                "**3. Auto-Debit (Recommended):**\n" +
                "• Set up auto-debit from your bank account\n" +
                "• Never miss an EMI payment\n" +
                "• Contact support to enable auto-debit\n\n" +
                "**Current Payment Due:**\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "💵 **Amount:** ₹15,000\n" +
                "📅 **Due Date:** 05-Dec-2024\n" +
                "⏰ **Days Remaining:** 12 days\n\n" +
                "**After Payment:**\n" +
                "• You will receive SMS and Email confirmation\n" +
                "• Payment will reflect in your account within 24 hours\n" +
                "• Updated loan status will be available\n\n" +
                "💬 **For payment assistance, type HELP or contact our support team.**";
    }
    
    /**
     * Get next EMI message
     */
    private String getNextEMIMessage() {
        return "📅 **Your Next EMI Details**\n\n" +
                "**Payment Information:**\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "💳 **EMI Amount:** ₹15,000\n" +
                "📅 **Due Date:** 05-Dec-2024\n" +
                "⏰ **Days Remaining:** 12 days\n" +
                "📊 **Status:** Upcoming\n\n" +
                "**EMI Breakdown:**\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "💰 **Principal Component:** ₹12,000\n" +
                "💵 **Interest Component:** ₹3,000\n" +
                "💳 **Total EMI:** ₹15,000\n\n" +
                "**Payment Reminders:**\n" +
                "• 📧 Email reminder: 3 days before due date\n" +
                "• 📱 SMS reminder: 1 day before due date\n" +
                "• 🔔 Push notification: On due date\n\n" +
                "**Quick Actions:**\n" +
                "• Type **PAY** to make payment now\n" +
                "• Type **STATUS** to check full loan status\n" +
                "• Type **REPAYMENT** to view complete schedule\n\n" +
                "💬 **Set up auto-debit to never miss a payment!**";
    }
    
    /**
     * Get loan details message
     */
    private String getLoanDetailsMessage() {
        return "📋 **Complete Loan Account Details**\n\n" +
                "**Loan Information:**\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "🆔 **Application ID:** APP12345678\n" +
                "💰 **Loan Amount:** ₹5,00,000\n" +
                "📊 **Interest Rate:** 12% per annum\n" +
                "⏰ **Tenure:** 36 months\n" +
                "💳 **EMI Amount:** ₹15,000 (approx.)\n" +
                "📅 **Disbursal Date:** 01-Nov-2024\n" +
                "📅 **Maturity Date:** 01-Nov-2027\n\n" +
                "**Current Status:**\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "✅ **Status:** Active\n" +
                "💵 **Amount Paid:** ₹15,000\n" +
                "📉 **Outstanding Principal:** ₹4,85,000\n" +
                "💳 **Total Outstanding:** ₹4,90,000\n" +
                "📊 **EMIs Paid:** 1 of 36\n" +
                "📅 **Next EMI Due:** 05-Dec-2024\n\n" +
                "**Account Summary:**\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "✅ **On-Time Payments:** 1\n" +
                "⚠️ **Overdue Payments:** 0\n" +
                "📊 **Payment History:** Excellent\n\n" +
                "**Quick Actions:**\n" +
                "• Type **STATUS** for current loan status\n" +
                "• Type **REPAYMENT** for payment schedule\n" +
                "• Type **PAY** to make a payment\n" +
                "• Type **HELP** for support\n\n" +
                "💬 **Thank you for being a valued customer!**";
    }
    
    /**
     * Get help message
     */
    private String getHelpMessage() {
        return "🆘 **How Can We Help You?**\n\n" +
                "**Available Services:**\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                "**Loan Management:**\n" +
                "• **STATUS** - Check loan balance and status\n" +
                "• **REPAYMENT** - View repayment schedule\n" +
                "• **PAY** - Make a payment\n" +
                "• **NEXT EMI** - View next EMI details\n" +
                "• **ACCOUNT** - Complete loan account details\n\n" +
                "**Support & Assistance:**\n" +
                "• Payment queries and issues\n" +
                "• Loan account modifications\n" +
                "• Auto-debit setup\n" +
                "• Early repayment options\n" +
                "• Loan statement requests\n\n" +
                "**Contact Information:**\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "📞 **Customer Care:** 1800-XXX-XXXX\n" +
                "📧 **Email:** support@lending.com\n" +
                "🕐 **Hours:** Mon-Sat, 9 AM - 6 PM\n" +
                "💬 **WhatsApp Support:** Available 24/7\n\n" +
                "**Common Queries:**\n" +
                "• How to make payment?\n" +
                "• When is my next EMI due?\n" +
                "• How to check loan balance?\n" +
                "• How to set up auto-debit?\n\n" +
                "💬 **Type any command above or contact our support team for assistance.**";
    }
    
    /**
     * Update stage for user
     */
    private String handleApplicationUpdate(String mobileId, String userInput) {
        return "Application update journey is currently in progress. Please hold on while we complete this step.";
    }
    
    private String handleDocumentSigning(String mobileId, String userInput) {
        // Check if input is empty
        if (userInput == null || userInput.trim().isEmpty()) {
            return "❌ **No response received.**\n\n" +
                    "To complete the document signing process, please provide your response.\n\n" +
                    "💬 **Please reply with 'SIGNED' once you have reviewed and signed the loan agreement documents.**";
        }
        
        String trimmedInput = userInput.trim();
        String upperInput = trimmedInput.toUpperCase();
        
        // Check for signed confirmation (case-insensitive)
        boolean isSigned = upperInput.equals("SIGNED") ||
                          upperInput.equals("SIGN") ||
                          upperInput.equals("DONE") ||
                          upperInput.equals("COMPLETE") ||
                          upperInput.equals("COMPLETED") ||
                          upperInput.startsWith("SIGNED,") ||
                          upperInput.startsWith("SIGNED ") ||
                          upperInput.contains("SIGNED");
        
        // Check for disbursal acceptance responses (case-insensitive)
        boolean acceptDisbursal = upperInput.equals("YES") ||
                                  upperInput.equals("Y") ||
                                  upperInput.equals("OK") ||
                                  upperInput.equals("OKAY") ||
                                  upperInput.equals("PROCEED") ||
                                  upperInput.equals("ACCEPT") ||
                                  upperInput.equals("AGREE") ||
                                  upperInput.startsWith("YES,") ||
                                  upperInput.startsWith("YES ");
        
        // Check for disbursal rejection responses (case-insensitive)
        boolean declineDisbursal = upperInput.equals("NO") ||
                                   upperInput.equals("N") ||
                                   upperInput.equals("DECLINE") ||
                                   upperInput.equals("REJECT") ||
                                   upperInput.equals("CANCEL") ||
                                   upperInput.equals("WAIT") ||
                                   upperInput.startsWith("NO,") ||
                                   upperInput.startsWith("NO ");
        
        // Priority: Check for signing confirmation first
        if (isSigned) {
            // User confirmed signing - ask for disbursal acknowledgment
            return "✅ **Thank you! Your signed documents have been received.**\n\n" +
                    "**Document Signing Completed Successfully**\n\n" +
                    "Your loan agreement documents have been verified and accepted.\n\n" +
                    "---\n\n" +
                    "**Ready for Loan Disbursal**\n\n" +
                    "We are now ready to disburse your loan amount of ₹5,00,000 to your registered bank account.\n\n" +
                    "**Disbursal Details:**\n" +
                    "• Loan Amount: ₹5,00,000\n" +
                    "• Processing Time: 2-3 business hours\n" +
                    "• You will receive SMS/Email notification upon successful disbursal\n\n" +
                    "💬 **Would you like to proceed with the loan disbursal now?**\n\n" +
                    "Please reply with 'YES' to proceed with disbursal or 'NO' if you need more time.\n\n" +
                    "---\n" +
                    "*By proceeding, you authorize us to disburse the loan amount to your registered account.*";
        }
        
        // Check for disbursal acceptance (user responding to disbursal question)
        if (acceptDisbursal) {
            // User accepted disbursal, move to disbursal stage
            updateStage(mobileId, ApplicationStages.DISBURSAL_IN_PROGRESS);
            return "✅ **Thank you for confirming!**\n\n" +
                    "**Loan Disbursal Initiated**\n\n" +
                    "Your loan disbursal request has been processed and approved.\n\n" +
                    "**Next Steps:**\n" +
                    "• Loan amount will be transferred to your registered bank account\n" +
                    "• Processing time: 2-3 business hours\n" +
                    "• You will receive SMS and Email notifications upon successful disbursal\n\n" +
                    "💬 **Please reply with 'CONFIRM' or 'OK' to acknowledge and complete the disbursal process.**\n\n" +
                    "---\n" +
                    "*You can track your loan status anytime by replying with 'STATUS'.*";
        }
        
        // Check for disbursal rejection
        if (declineDisbursal) {
            // User declined disbursal for now
            return "**Thank you for your response.**\n\n" +
                    "We understand you need more time before proceeding with the loan disbursal.\n\n" +
                    "**No worries!** You can proceed with the disbursal anytime by replying with 'YES' when you're ready.\n\n" +
                    "If you have any questions or need assistance, please feel free to reach out to us.\n\n" +
                    "Is there anything else we can help you with today?";
        }
        
        // Invalid response - show error with instructions
        return "❌ **Invalid response. Please provide a valid confirmation.**\n\n" +
                "**Please reply with one of the following:**\n" +
                "• 'SIGNED' - to confirm you have signed the documents\n" +
                "• 'YES' - to proceed with loan disbursal (if documents are already signed)\n" +
                "• 'NO' - if you need more time before disbursal\n\n" +
                "💬 **Your response will help us proceed with the next steps in your loan application.**";
    }
    
    private void updateStage(String mobileId, ApplicationStages newStage) {
        Optional<UserDetails> userDetailsOpt = userDetailsRepository.findById(mobileId);
        if (userDetailsOpt.isPresent()) {
            UserDetails userDetails = userDetailsOpt.get();
            userDetails.setCurrentStage(newStage.name());
            userDetailsRepository.save(userDetails);
            logger.info("Updated stage for {} to {}", mobileId, newStage);
        } else {
            logger.warn("Unable to update stage for {} because user details do not exist", mobileId);
        }
    }
}

