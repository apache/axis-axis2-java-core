
    /**
     * BankServiceSkeleton.java
     *
     */
    package example;
    /**
     *  BankServiceSkeleton java skeleton for the axisService
     */
    public class BankServiceSkeleton{


        /**

          * @param param0

         */
        public  example.WithdrawResponse withdraw(example.Withdraw param0)
           throws InsufficientFundFaultMessageException,AccountNotExistFaultMessageException{
                final String account = param0.getAccount();
        if (account.equals("13")) {
            final AccountNotExistFault fault = new AccountNotExistFault();
            fault.setAccount(account);
            AccountNotExistFaultMessageException messageException = new AccountNotExistFaultMessageException("Account does not exist!");
            messageException.setFaultMessage(fault);
            throw messageException;
        }

        final int amount = param0.getAmount();
        if (amount > 1000) {
            final InsufficientFundFault fault = new InsufficientFundFault();
            fault.setAccount(account);
            fault.setBalance(1000);
            fault.setRequestedFund(amount);
            InsufficientFundFaultMessageException messageException = new InsufficientFundFaultMessageException("Insufficient funds");
            messageException.setFaultMessage(fault);
            throw messageException;
        }

        final WithdrawResponse response = new WithdrawResponse();
        response.setBalance(1000 - amount);
        return response;
        }

    }

