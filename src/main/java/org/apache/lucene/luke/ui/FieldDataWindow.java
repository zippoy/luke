package org.apache.lucene.luke.ui;

import org.apache.lucene.analysis.payloads.PayloadHelper;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.luke.core.Util;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Map;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.*;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Date;

public class FieldDataWindow extends Dialog implements Bindable {

  @BXML
  private Label name;
  @BXML
  private Label length;
  @BXML
  private Spinner cDecoder;
  @BXML
  private Label error;
  @BXML
  private TextArea data;

  private Resources resources;

  private IndexableField field;

  @Override
  public void initialize(Map<String, Object> map, URL url, Resources resources) {
    this.resources = resources;
  }

  public void initFieldData(String fieldName, IndexableField field) {
    this.field = field;

    setContentDecoders();

    name.setText(fieldName);
    ContentDecoder dec = ContentDecoder.defDecoder();
    dec.decode(field);
    data.setText(String.valueOf(dec.value));
    length.setText(Integer.toString(dec.len));
  }

  private void setContentDecoders() {
    ArrayList<Object> decoders = new ArrayList<Object>();
    ContentDecoder[] contentDecoders = ContentDecoder.values();
    for (int i = contentDecoders.length - 1; i >= 0; i--) {
      decoders.add(contentDecoders[i]);
    }
    cDecoder.setSpinnerData(decoders);
    cDecoder.setSelectedItem(ContentDecoder.STRING_UTF8);

    cDecoder.getSpinnerSelectionListeners().add(new SpinnerSelectionListener.Adapter() {
      @Override
      public void selectedItemChanged(Spinner spinner, Object o) {
        ContentDecoder dec = (ContentDecoder) spinner.getSelectedItem();
        if (dec == null) {
          dec = ContentDecoder.defDecoder();
        }
        dec.decode(field);
        data.setText(dec.value);
        length.setText(Integer.toString(dec.len));
        if (dec.warn) {
          error.setVisible(true);
          try {
            data.setStyles("{color:'#bdbdbd'}");
          } catch (SerializationException e) {
            e.printStackTrace();
          }
          data.setEnabled(false);
        } else {
          error.setVisible(false);
          try {
            data.setStyles("{color:'#000000'}");
          } catch (SerializationException e) {
            e.printStackTrace();
          }
          data.setEnabled(true);
        }
      }
    });
  }


  enum ContentDecoder {
    STRING_UTF8("String UTF-8"),
    STRING("String default enc."),
    HEXDUMP("Hexdump"),
    DATETIME("Date / Time"),
    NUMERIC("Numeric"),
    LONG("Long (prefix-coded)"),
    ARRAY_OF_INT("Array of int"),
    ARRAY_OF_FLOAT("Array of float");

    private String strExpr;
    ContentDecoder(String strExpr) {
      this.strExpr = strExpr;
    }

    @Override
    public String toString() {
      return strExpr;
    }

    public static ContentDecoder defDecoder() {
      return STRING_UTF8;
    }

    String value = "";  // decoded value
    int len;       // length of decoded value
    boolean warn;  // set to true if decode failed

    public void decode(IndexableField field) {
      if (field == null) {
        return ;
      }
      warn = false;
      byte[] data = null;
      if (field.binaryValue() != null) {
        BytesRef bytes = field.binaryValue();
        data = new byte[bytes.length];
        System.arraycopy(bytes.bytes, bytes.offset, data, 0,
          bytes.length);
      }
      else if (field.stringValue() != null) {
        try {
          data = field.stringValue().getBytes("UTF-8");
        } catch (UnsupportedEncodingException uee) {
          warn = true;
          uee.printStackTrace();
          data = field.stringValue().getBytes();
        }
      }
      if (data == null) data = new byte[0];

      switch(this) {
        case STRING_UTF8:
          value = field.stringValue();
          if (value != null) len = value.length();
          break;
        case STRING:
          value = new String(data);
          len = value.length();
          break;
        case HEXDUMP:
          value = Util.bytesToHex(data, 0, data.length, true);
          len = data.length;
          break;
        case DATETIME:
          try {
            Date d = DateTools.stringToDate(field.stringValue());
            value = d.toString();
            len = 1;
          } catch (Exception e) {
            warn = true;
            value = Util.bytesToHex(data, 0, data.length, true);
          }
          break;
        case NUMERIC:
          if (field.numericValue() != null) {
            value = field.numericValue().toString() + " (" + field.numericValue().getClass().getSimpleName() + ")";
          } else {
            warn = true;
            value = Util.bytesToHex(data, 0, data.length, true);
          }
          break;
        case LONG:
          try {
            long num = NumericUtils.sortableBytesToLong(new BytesRef(field.stringValue()).bytes, 0);
            value = String.valueOf(num);
            len = 1;
          } catch (Exception e) {
            warn = true;
            value = Util.bytesToHex(data, 0, data.length, true);
          }
          break;
        case ARRAY_OF_INT:
          if (data.length % 4 == 0) {
            len = data.length / 4;
            StringBuilder sb = new StringBuilder();
            for (int k = 0; k < data.length; k += 4) {
              if (k > 0) sb.append(',');
              sb.append(String.valueOf(PayloadHelper.decodeInt(data, k)));
            }
            value = sb.toString();
          } else {
            warn = true;
            value = Util.bytesToHex(data, 0, data.length, true);
          }
          break;
        case ARRAY_OF_FLOAT:
          if (data.length % 4 == 0) {
            len = data.length / 4;
            StringBuilder sb = new StringBuilder();
            for (int k = 0; k < data.length; k += 4) {
              if (k > 0) sb.append(',');
              sb.append(String.valueOf(PayloadHelper.decodeFloat(data, k)));
            }
            value = sb.toString();
          } else {
            warn = true;
            value = Util.bytesToHex(data, 0, data.length, true);
          }
          break;
      }
    }

  }
}
