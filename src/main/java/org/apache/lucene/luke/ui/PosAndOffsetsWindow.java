package org.apache.lucene.luke.ui;

import org.apache.lucene.analysis.payloads.PayloadHelper;
import org.apache.lucene.index.DocsAndPositionsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.luke.core.Util;
import org.apache.lucene.util.BytesRef;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.*;

import java.net.URL;

public class PosAndOffsetsWindow extends Dialog implements Bindable {

  @BXML
  private TableView posTable;
  @BXML
  private Label docNum;
  @BXML
  private Label term;
  @BXML
  private Label tf;
  @BXML
  private Label offsets;
  @BXML
  private Spinner pDecoder;
  @BXML
  private PushButton posCopyButton;

  private Resources resources;

  private List<PositionAndOffset> tableData;

  @Override
  public void initialize(Map<String, Object> map, URL url, Resources resources) {
    this.resources = resources;
  }

  public void initPositionInfo(DocsAndPositionsEnum pe, Term lastTerm) throws Exception {
    setPayloadDecoders();
    tableData = new ArrayList<PositionAndOffset>(getTermPositionAndOffsets(pe));
    docNum.setText(String.valueOf(pe.docID()));
    term.setText(lastTerm.field() + ":" + lastTerm.text());
    tf.setText(String.valueOf(pe.freq()));
    if (!tableData.isEmpty()) {
      offsets.setText(String.valueOf(tableData.get(0).hasOffsets));
    }
    posTable.setTableData(tableData);
    addPushButtonListener();
  }

  private void setPayloadDecoders() {
    ArrayList<Object> decoders = new ArrayList<Object>();
    decoders.add(PayloadDecoder.ARRAY_OF_FLOAT);
    decoders.add(PayloadDecoder.ARRAY_OF_INT);
    decoders.add(PayloadDecoder.HEXDUMP);
    decoders.add(PayloadDecoder.STRING);
    decoders.add(PayloadDecoder.STRING_UTF8);
    pDecoder.setSpinnerData(decoders);
    pDecoder.setSelectedItem(PayloadDecoder.STRING_UTF8);

    pDecoder.getSpinnerSelectionListeners().add(new SpinnerSelectionListener.Adapter() {
      @Override
      public void selectedItemChanged(Spinner spinner, Object o) {
        try {
          for (PositionAndOffset row : tableData) {
            PayloadDecoder dec = (PayloadDecoder) spinner.getSelectedItem();
            if (dec == null) {
              dec = PayloadDecoder.defDecoder();
            }
            row.payloadStr = dec.decode(row.payload);
          }
          posTable.repaint();  // update table data
        } catch (Exception e) {
          // TODO:
          e.printStackTrace();
        }
      }
    });
  }

  public class PositionAndOffset {
    public int pos = -1;
    public boolean hasOffsets = false;
    public String offsets = "----";
    public BytesRef payload = null;
    public String payloadStr = "----";
  }

  private PositionAndOffset[] getTermPositionAndOffsets(DocsAndPositionsEnum pe) throws Exception {
    int freq = pe.freq();

    PositionAndOffset[] res = new PositionAndOffset[freq];
    for (int i = 0; i < freq; i++) {
      PositionAndOffset po = new PositionAndOffset();
      po.pos = pe.nextPosition();
      if (pe.startOffset() >= 0 && pe.endOffset() >= 0) {
        // retrieve start and end offsets
        po.hasOffsets = true;
        po.offsets = String.valueOf(pe.startOffset()) + " - " + String.valueOf(pe.endOffset());
      }
      if (pe.getPayload() != null) {
        po.payload = pe.getPayload();
        po.payloadStr = ((PayloadDecoder) pDecoder.getSelectedItem()).decode(pe.getPayload());
      }
      res[i] = po;
    }
    return res;
  }

  enum PayloadDecoder {
    STRING_UTF8("String UTF-8"),
    STRING("String default enc."),
    HEXDUMP("Hexdump"),
    ARRAY_OF_INT("Array of int"),
    ARRAY_OF_FLOAT("Array of float");

    private String strExpr = null;
    PayloadDecoder(String expr) {
      this.strExpr = expr;
    }

    @Override
    public String toString() {
      return strExpr;
    }

    public static PayloadDecoder defDecoder() {
      return STRING_UTF8;
    }

    public String decode(BytesRef payload) {
      String val = "----";
      StringBuilder sb = null;
      if (payload == null) {
        return val;
      }
      switch(this) {
        case STRING_UTF8:
          try {
            val = new String(payload.bytes, payload.offset, payload.length, "UTF-8");
          } catch (Exception e) {
            e.printStackTrace();
            val = new String(payload.bytes, payload.offset, payload.length);
          }
          break;
        case STRING:
          val = new String(payload.bytes, payload.offset, payload.length);
          break;
        case HEXDUMP:
          val = Util.bytesToHex(payload.bytes, payload.offset, payload.length, false);
          break;
        case ARRAY_OF_INT:
          sb = new StringBuilder();
          for (int k = payload.offset; k < payload.offset + payload.length; k += 4) {
            if (k > 0) sb.append(',');
            sb.append(String.valueOf(PayloadHelper.decodeInt(payload.bytes, k)));
          }
          val = sb.toString();
          break;
        case ARRAY_OF_FLOAT:
          sb = new StringBuilder();
          for (int k = payload.offset; k < payload.offset + payload.length; k += 4) {
            if (k > 0) sb.append(',');
            sb.append(String.valueOf(PayloadHelper.decodeFloat(payload.bytes, k)));
          }
          val = sb.toString();
          break;
      }
      return val;
    }
  }

  private void addPushButtonListener() {

    posCopyButton.getButtonPressListeners().add(new ButtonPressListener() {
      @Override
      public void buttonPressed(Button button) {
        // fired when 'Copy to Clipboard' button pressed
        Sequence<PositionAndOffset> selectedRows = (Sequence<PositionAndOffset>) posTable.getSelectedRows();
        if (selectedRows == null || selectedRows.getLength() == 0) {
          Alert.alert(MessageType.INFO, "No rows selected.", getWindow());
        } else {
          StringBuilder sb = new StringBuilder();
          for (int i = 0; i < selectedRows.getLength(); i++) {
            PositionAndOffset row = selectedRows.get(i);
            sb.append(row.pos + "\t");
            sb.append(row.offsets + "\t");
            sb.append(row.payloadStr);
            if (i < selectedRows.getLength() - 1) {
              sb.append("\n");
            }
          }
          LocalManifest content = new LocalManifest();
          content.putText(sb.toString());
          Clipboard.setContent(content);
        }
      }
    });
  }

}
