package com.google.sitebricks.compiler;

import org.mvel2.ErrorDetail;

/**
 * Represents a template compile error or warning (may be due to an MVEL compile failure or a
 * sitebricks static check failure.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public abstract class CompileError {

  public abstract String getFragment();

  public abstract int getLine();

  public abstract CompileErrors getReason();

  public abstract EvaluatorCompiler.CompileErrorDetail getCause();

  public static CompileErrorBuilder in(String fragment) {
    return new Builder(fragment);
  }

  public static interface CompileErrorBuilder {
    CompileErrorBuilder near(int line);

    CompileError causedBy(ExpressionCompileException e);

    CompileError causedBy(CompileErrors reason);

    CompileError causedBy(CompileErrors reason, ExpressionCompileException e);

    CompileError causedBy(CompileErrors reason, String cause);
  }

  private static class Builder implements CompileErrorBuilder {
    private final String fragment;
    private int line;

    private Builder(String fragment) {
      this.fragment = fragment;
    }

    public CompileErrorBuilder near(int line) {
      this.line = line;

      return this;
    }

    public CompileError causedBy(ExpressionCompileException e) {
      return new CompileErrorImpl(fragment, line, e.getError());
    }

    public CompileError causedBy(CompileErrors reason) {
      return new CompileErrorImpl(fragment, line, reason);
    }

    public CompileError causedBy(CompileErrors reason, ExpressionCompileException e) {
      return new CompileErrorImpl(fragment, line, e.getError(), reason);
    }

    public CompileError causedBy(CompileErrors reason, String cause) {
      return new CompileErrorImpl(fragment, line,
          new EvaluatorCompiler.CompileErrorDetail(cause, new ErrorDetail(fragment.toCharArray(), line, true, cause)), reason);
    }
  }

  private static class CompileErrorImpl extends CompileError {
    private final String fragment;
    private final int line;
    private final EvaluatorCompiler.CompileErrorDetail error;
    private final CompileErrors reason;

    public CompileErrorImpl(String fragment, int line, EvaluatorCompiler.CompileErrorDetail error) {
      this.fragment = fragment;
      this.line = line;
      this.error = error;

      this.reason = CompileErrors.ILLEGAL_EXPRESSION;
    }

    public CompileErrorImpl(String fragment, int line, CompileErrors reason) {
      this.fragment = fragment;
      this.line = line;
      this.reason = reason;

      this.error = null;
    }

    public CompileErrorImpl(String fragment, int line, EvaluatorCompiler.CompileErrorDetail error,
                            CompileErrors reason) {

      this.fragment = fragment;
      this.line = line;
      this.error = error;
      this.reason = reason;
    }

    @Override
    public String getFragment() {
      return fragment;
    }

    @Override
    public int getLine() {
      return line;
    }

    @Override
    public CompileErrors getReason() {
      return reason;
    }

    @Override
    public EvaluatorCompiler.CompileErrorDetail getCause() {
      return error;
    }

    @Override
    public String toString() {
      //TODO make this nicer?
      return reason.toString();
    }
  }
}
