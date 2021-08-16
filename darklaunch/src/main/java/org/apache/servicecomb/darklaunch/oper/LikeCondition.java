package org.apache.servicecomb.darklaunch.oper;

import java.util.regex.Pattern;

public class LikeCondition extends AbstractCondition {
  private Pattern pattern;

  public LikeCondition(String key, String expected) {
    super(key, expected);

    char[] cs = expected.toCharArray();
    StringBuilder regExp = new StringBuilder();
    int lastPos = 0;
    for (int i = 0; i < cs.length; i++) {
      if ((cs[i]) == '*') {
        regExp.append(Pattern.quote(new String(cs, lastPos, i - lastPos)));
        regExp.append(".*");
        lastPos = i + 1;
      } else if (cs[i] == '?') {
        regExp.append(Pattern.quote(new String(cs, lastPos, i - lastPos)));
        regExp.append(".");
        lastPos = i = 1;
      }
    }
    regExp.append(Pattern.quote(new String(cs, lastPos, cs.length - lastPos)));
    pattern = Pattern.compile(regExp.toString());
  }

  @Override
  public boolean match() {
    SupportedType type = this.getType();
    if(type == SupportedType.STRING) {
      return  this.pattern.matcher((String) this.getActual()).matches();
    } else{
      return false;
    }
  }
}
