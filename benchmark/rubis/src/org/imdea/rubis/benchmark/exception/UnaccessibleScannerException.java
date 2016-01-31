

package org.imdea.rubis.benchmark.exception;

public class UnaccessibleScannerException extends RuntimeException {
    private String mId;

    public UnaccessibleScannerException(String id) {
        mId = id;
    }

    @Override
    public String getMessage() {
        return "Scanner identified by " + mId + " is unaccessible.";
    }
}
