/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.demo;

import lombok.Getter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Control;

@Getter
public class FormDatas { // NOSONAR

  static final int DENOMINATOR = 100;

  private final FormData formData;

  public static FormDatas attach( Control control ) { // NOSONAR
    return new FormDatas( control );
  }

  public FormDatas atLeft() { // NOSONAR
    return atLeft( 0 );
  }

  public FormDatas atLeft( int margin ) { // NOSONAR
    formData.left = new FormAttachment( 0, margin );
    return this;
  }

  public FormDatas atLeftTo( Control control ) { // NOSONAR
    return atLeftTo( control, 0 );
  }

  public FormDatas atLeftTo( Control control, int margin ) { // NOSONAR
    return atLeftTo( control, margin, SWT.DEFAULT );
  }

  public FormDatas atLeftTo( Control control, int margin, int alignment ) { // NOSONAR
    formData.left = new FormAttachment( control, margin, alignment );
    return this;
  }

  public FormDatas fromLeft( int numerator ) { // NOSONAR
    return fromLeft( numerator, 0 );
  }

  public FormDatas fromLeft( int numerator, int margin ) { // NOSONAR
    formData.left = new FormAttachment( numerator, margin );
    return this;
  }

  public FormDatas atRight() { // NOSONAR
    return atRight( 0 );
  }

  public FormDatas atRight( int margin ) { // NOSONAR
    formData.right = new FormAttachment( DENOMINATOR, -margin );
    return this;
  }

  public FormDatas atRightTo( Control control ) { // NOSONAR
    atRightTo( control, 0 );
    return this;
  }

  public FormDatas atRightTo( Control control, int margin ) { // NOSONAR
    return atRightTo( control, margin, SWT.DEFAULT );
  }

  public FormDatas atRightTo( Control control, int margin, int alignment ) { // NOSONAR
    formData.right = new FormAttachment( control, -margin, alignment );
    return this;
  }

  public FormDatas fromRight( int numerator ) { // NOSONAR
    return fromRight( numerator, 0 );
  }

  public FormDatas fromRight( int numerator, int margin ) { // NOSONAR
    formData.right = new FormAttachment( DENOMINATOR - numerator, -margin );
    return this;
  }

  public FormDatas atTop() { // NOSONAR
    return atTop( 0 );
  }

  public FormDatas atTop( int margin ) { // NOSONAR
    formData.top = new FormAttachment( 0, margin );
    return this;
  }

  public FormDatas atTopTo( Control control ) { // NOSONAR
    return atTopTo( control, 0 );
  }

  public FormDatas atTopTo( Control control, int margin ) { // NOSONAR
    return atTopTo( control, margin, SWT.DEFAULT );
  }

  public FormDatas atTopTo( Control control, int margin, int alignment ) { // NOSONAR
    formData.top = new FormAttachment( control, margin, alignment );
    return this;
  }

  public FormDatas fromTop( int numerator ) { // NOSONAR
    return fromTop( numerator, 0 );
  }

  public FormDatas fromTop( int numerator, int margin ) { // NOSONAR
    formData.top = new FormAttachment( numerator, margin );
    return this;
  }

  public FormDatas atBottom() { // NOSONAR
    return atBottom( 0 );
  }

  public FormDatas atBottom( int margin ) { // NOSONAR
    formData.bottom = new FormAttachment( DENOMINATOR, -margin );
    return this;
  }

  public FormDatas atBottomTo( Control control ) { // NOSONAR
    return atBottomTo( control, 0 );
  }

  public FormDatas atBottomTo( Control control, int margin ) { // NOSONAR
    return atBottomTo( control, margin, SWT.DEFAULT );
  }

  public FormDatas atBottomTo( Control control, int margin, int alignment ) { // NOSONAR
    formData.bottom = new FormAttachment( control, -margin, alignment );
    return this;
  }

  public FormDatas fromBottom( int numerator ) { // NOSONAR
    return fromBottom( numerator, 0 );
  }

  public FormDatas fromBottom( int numerator, int margin ) { // NOSONAR
    formData.bottom = new FormAttachment( DENOMINATOR - numerator, -margin );
    return this;
  }

  public FormDatas withWidth( int width ) { // NOSONAR
    formData.width = width;
    return this;
  }

  public FormDatas withHeight( int height ) { // NOSONAR
    formData.height = height;
    return this;
  }

  private FormDatas(Control control ) {
    formData = new FormData();
    control.setLayoutData( formData );
  }
}
