package com.alimmit.golf.utils;

import com.alimmit.golf.GlobalConstants;

/** Build some standard personas to use in MockMvc tests */
public interface JwtPersona {

  String name();

  String sub();

  JwtPersona GARY_GOLFER = new JwtPersonaImpl("Gary Golfer", "123");
  JwtPersona PAT_PUTTER = new JwtPersonaImpl("Pat Putter", "234");
  JwtPersona DANA_DRIVER = new JwtPersonaImpl("Dana Driver", "345");
  JwtPersona AMY_ADMIN = new JwtPersonaImpl("Amy Admin", "456");

  static JwtClaimApplier forGaryGolfer() {
    return forGaryGolfer(DEFAULT_SCOPES);
  }

  static JwtClaimApplier forPatPutter() {
    return forPatPutter(DEFAULT_SCOPES);
  }

  static JwtClaimApplier forDanaDriver() {
    return forDanaDriver(DEFAULT_SCOPES);
  }

  static JwtClaimApplier forGaryGolfer(String... scopes) {
    return new JwtClaimApplierImpl(GARY_GOLFER.name(), GARY_GOLFER.sub(), scopes);
  }

  static JwtClaimApplier forPatPutter(String... scopes) {
    return new JwtClaimApplierImpl(PAT_PUTTER.name(), PAT_PUTTER.sub(), scopes);
  }

  static JwtClaimApplier forDanaDriver(String... scopes) {
    return new JwtClaimApplierImpl(DANA_DRIVER.name(), DANA_DRIVER.sub(), scopes);
  }

  static JwtClaimApplier forAmyAdmin() {
    return forAmyAdmin(ALL_SCOPES);
  }

  static JwtClaimApplier forAmyAdmin(String... scopes) {
    return new JwtClaimApplierImpl(AMY_ADMIN.name(), AMY_ADMIN.sub(), scopes);
  }

  String SCOPE_READ_SCORECARD =
      GlobalConstants.SCOPE_PERMISSION_READ + ":" + GlobalConstants.SCOPE_SCORECARD;
  String SCOPE_WRITE_SCORECARD =
      GlobalConstants.SCOPE_PERMISSION_WRITE + ":" + GlobalConstants.SCOPE_SCORECARD;
  String SCOPE_READ_HANDICAP =
      GlobalConstants.SCOPE_PERMISSION_READ + ":" + GlobalConstants.SCOPE_HANDICAP;
  String SCOPE_READ_COURSE =
      GlobalConstants.SCOPE_PERMISSION_READ + ":" + GlobalConstants.SCOPE_COURSE;
  String SCOPE_WRITE_COURSE =
      GlobalConstants.SCOPE_PERMISSION_WRITE + ":" + GlobalConstants.SCOPE_COURSE;

  String[] ALL_SCOPES = {
    SCOPE_READ_SCORECARD,
    SCOPE_WRITE_SCORECARD,
    SCOPE_READ_HANDICAP,
    SCOPE_READ_COURSE,
    SCOPE_WRITE_COURSE
  };

  String[] DEFAULT_SCOPES = {
    SCOPE_READ_SCORECARD, SCOPE_WRITE_SCORECARD, SCOPE_READ_HANDICAP, SCOPE_READ_COURSE
  };
}
