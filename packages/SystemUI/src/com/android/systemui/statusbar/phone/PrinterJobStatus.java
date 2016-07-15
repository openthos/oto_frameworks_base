package com.android.systemui.statusbar.phone;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Describe a printer job's status
 */
public class PrinterJobStatus implements Parcelable {

    /**
     * Add more when we found a new state.
     * STATUS_READY                 Ready to print , may be in the queue.
     * STATUS_PRINTING              Printing.
     * STATUS_HOLDING               Pause/Hold.
     * STATUS_ERROR                 Error occurred , more info in ERROR variable.
     * STATUS_WAITING_FOR_PRINTER   Waiting for printing available.
     */
    public static final int STATUS_READY = 1;
    public static final int STATUS_PRINTING = 2;
    public static final int STATUS_HOLDING = 3;
    public static final int STATUS_ERROR = 4;
    public static final int STATUS_WAITING_FOR_PRINTER = 5;

    private String mFileName;
    private String mPrinter;
    private int mStatus;
    private String mSize;
    private int mJobId;
    private String ERROR = "";

    public PrinterJobStatus() {
    }

    public String getFileName() {
        return mFileName;
    }

    public void setFileName(String fileName) {
        mFileName = fileName;
    }

    public String getPrinter() {
        return mPrinter;
    }

    public void setPrinter(String printer) {
        mPrinter = printer;
    }

    /**
     * Need to read ERROR variable manually for more details
     * when the mStatus is STATUS_ERROR or STATUS_PRINTING.
     *
     * @return mStatus
     */
    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int status) {
        mStatus = status;
    }

    public String getSize() {
        return mSize;
    }

    public void setSize(String size) {
        mSize = size;
    }

    public int getJobId() {
        return mJobId;
    }

    public void setJobId(int jobId) {
        mJobId = jobId;
    }

    public String getERROR() {
        return ERROR;
    }

    public void setERROR(String ERROR) {
        ERROR = ERROR;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PrinterJobStatus jobItem = (PrinterJobStatus) o;

        if (mStatus != jobItem.mStatus) {
            return false;
        }
        if (mJobId != jobItem.mJobId) {
            return false;
        }
        if (mFileName != null ? !mFileName.equals(jobItem.mFileName) : jobItem.mFileName != null) {
            return false;
        }
        if (mPrinter != null ? !mPrinter.equals(jobItem.mPrinter) : jobItem.mPrinter != null) {
            return false;
        }
        if (mSize != null ? !mSize.equals(jobItem.mSize) : jobItem.mSize != null) {
            return false;
        }
        return ERROR != null ? ERROR.equals(jobItem.ERROR) : jobItem.ERROR == null;

    }

    @Override
    public int hashCode() {
        int result = mFileName != null ? mFileName.hashCode() : 0;
        result = 31 * result + (mPrinter != null ? mPrinter.hashCode() : 0);
        result = 31 * result + mStatus;
        result = 31 * result + (mSize != null ? mSize.hashCode() : 0);
        result = 31 * result + mJobId;
        result = 31 * result + (ERROR != null ? ERROR.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PrinterJobStatus{" +
                "fileName='" + mFileName + '\'' +
                ", printer='" + mPrinter + '\'' +
                ", status=" + mStatus +
                ", size='" + mSize + '\'' +
                ", jobId=" + mJobId +
                ", ERROR='" + ERROR + '\'' +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mFileName);
        dest.writeString(mPrinter);
        dest.writeInt(mStatus);
        dest.writeString(mSize);
        dest.writeInt(mJobId);
        dest.writeString(ERROR);
    }

    protected PrinterJobStatus(Parcel in) {
        mFileName = in.readString();
        mPrinter = in.readString();
        mStatus = in.readInt();
        mSize = in.readString();
        mJobId = in.readInt();
        ERROR = in.readString();
    }

    public static final Creator<PrinterJobStatus> CREATOR = new Creator<PrinterJobStatus>() {
        @Override
        public PrinterJobStatus createFromParcel(Parcel source) {
            return new PrinterJobStatus(source);
        }

        @Override
        public PrinterJobStatus[] newArray(int size) {
            return new PrinterJobStatus[size];
        }
    };
}