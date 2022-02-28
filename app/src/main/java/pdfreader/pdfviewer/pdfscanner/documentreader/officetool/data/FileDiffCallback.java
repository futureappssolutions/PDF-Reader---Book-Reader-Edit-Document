package pdfreader.pdfviewer.pdfscanner.documentreader.officetool.data;

import androidx.recyclerview.widget.DiffUtil;

import pdfreader.pdfviewer.pdfscanner.documentreader.officetool.GetSet.PdfDataType;

import java.util.List;

public class FileDiffCallback extends DiffUtil.Callback {
    private List<PdfDataType> newPDFDataTypeList;
    private List<PdfDataType> oldPDFDataTypeList;

    public FileDiffCallback(List<PdfDataType> list, List<PdfDataType> list2) {
        this.oldPDFDataTypeList = list;
        this.newPDFDataTypeList = list2;
    }

    @Override
    public int getOldListSize() {
        return this.oldPDFDataTypeList.size();
    }

    @Override
    public int getNewListSize() {
        return this.newPDFDataTypeList.size();
    }

    @Override
    public boolean areItemsTheSame(int i, int i2) {
        return this.oldPDFDataTypeList.get(i).getAbsolutePath().equals(this.newPDFDataTypeList.get(i2).getAbsolutePath());
    }

    @Override
    public boolean areContentsTheSame(int i, int i2) {
        return this.oldPDFDataTypeList.get(i).equals(this.newPDFDataTypeList.get(i2));
    }

    @Override
    public Object getChangePayload(int i, int i2) {
        return super.getChangePayload(i, i2);
    }
}
