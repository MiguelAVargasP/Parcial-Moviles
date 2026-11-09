/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Using: C:\Users\PERSONAL\AppData\Local\Android\Sdk\build-tools\35.0.0\aidl.exe -pC:\Users\PERSONAL\AppData\Local\Android\Sdk\platforms\android-35\framework.aidl -oD:\Users\PERSONAL\Downloads\ArcadeUPB\core\build\generated\aidl_source_output_dir\debug\out -ID:\Users\PERSONAL\Downloads\ArcadeUPB\core\src\main\aidl -ID:\Users\PERSONAL\Downloads\ArcadeUPB\core\src\debug\aidl -IC:\Users\PERSONAL\.gradle\caches\8.14.3\transforms\32a392ce01d2d8b47a2a53ff9421f5e5\transformed\core-1.16.0\aidl -IC:\Users\PERSONAL\.gradle\caches\8.14.3\transforms\817884e96618917d5549d9a03865e541\transformed\versionedparcelable-1.1.1\aidl -dC:\Users\PERSONAL\AppData\Local\Temp\aidl11909115991931413347.d D:\Users\PERSONAL\Downloads\ArcadeUPB\core\src\main\aidl\edu\\upb\arcade\core\IArcadeService.aidl
 */
package edu.upb.arcade.core;
public interface IArcadeService extends android.os.IInterface
{
  /** Default implementation for IArcadeService. */
  public static class Default implements edu.upb.arcade.core.IArcadeService
  {
    /** Nickname del usuario con sesión iniciada en el hub, o null si nadie ha iniciado sesión. */
    @Override public java.lang.String getNickname() throws android.os.RemoteException
    {
      return null;
    }
    /** Mejor score del usuario actual en ese juego (0 si no tiene). */
    @Override public int getMejorScore(java.lang.String juego) throws android.os.RemoteException
    {
      return 0;
    }
    /**
     * Guarda un score para el usuario actual. El hub agrega la ciudad (GPS) por su cuenta.
     * Devuelve false si no hay sesión o el juego no existe.
     */
    @Override public boolean guardarScore(java.lang.String juego, int puntos) throws android.os.RemoteException
    {
      return false;
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements edu.upb.arcade.core.IArcadeService
  {
    /** Construct the stub at attach it to the interface. */
    @SuppressWarnings("this-escape")
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an edu.upb.arcade.core.IArcadeService interface,
     * generating a proxy if needed.
     */
    public static edu.upb.arcade.core.IArcadeService asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof edu.upb.arcade.core.IArcadeService))) {
        return ((edu.upb.arcade.core.IArcadeService)iin);
      }
      return new edu.upb.arcade.core.IArcadeService.Stub.Proxy(obj);
    }
    @Override public android.os.IBinder asBinder()
    {
      return this;
    }
    @Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
    {
      java.lang.String descriptor = DESCRIPTOR;
      if (code >= android.os.IBinder.FIRST_CALL_TRANSACTION && code <= android.os.IBinder.LAST_CALL_TRANSACTION) {
        data.enforceInterface(descriptor);
      }
      if (code == INTERFACE_TRANSACTION) {
        reply.writeString(descriptor);
        return true;
      }
      switch (code)
      {
        case TRANSACTION_getNickname:
        {
          java.lang.String _result = this.getNickname();
          reply.writeNoException();
          reply.writeString(_result);
          break;
        }
        case TRANSACTION_getMejorScore:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          int _result = this.getMejorScore(_arg0);
          reply.writeNoException();
          reply.writeInt(_result);
          break;
        }
        case TRANSACTION_guardarScore:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          int _arg1;
          _arg1 = data.readInt();
          boolean _result = this.guardarScore(_arg0, _arg1);
          reply.writeNoException();
          reply.writeInt(((_result)?(1):(0)));
          break;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
      return true;
    }
    private static class Proxy implements edu.upb.arcade.core.IArcadeService
    {
      private android.os.IBinder mRemote;
      Proxy(android.os.IBinder remote)
      {
        mRemote = remote;
      }
      @Override public android.os.IBinder asBinder()
      {
        return mRemote;
      }
      public java.lang.String getInterfaceDescriptor()
      {
        return DESCRIPTOR;
      }
      /** Nickname del usuario con sesión iniciada en el hub, o null si nadie ha iniciado sesión. */
      @Override public java.lang.String getNickname() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        java.lang.String _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getNickname, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readString();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      /** Mejor score del usuario actual en ese juego (0 si no tiene). */
      @Override public int getMejorScore(java.lang.String juego) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(juego);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getMejorScore, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readInt();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      /**
       * Guarda un score para el usuario actual. El hub agrega la ciudad (GPS) por su cuenta.
       * Devuelve false si no hay sesión o el juego no existe.
       */
      @Override public boolean guardarScore(java.lang.String juego, int puntos) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        boolean _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(juego);
          _data.writeInt(puntos);
          boolean _status = mRemote.transact(Stub.TRANSACTION_guardarScore, _data, _reply, 0);
          _reply.readException();
          _result = (0!=_reply.readInt());
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
    }
    static final int TRANSACTION_getNickname = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_getMejorScore = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_guardarScore = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
  }
  /** @hide */
  public static final java.lang.String DESCRIPTOR = "edu.upb.arcade.core.IArcadeService";
  /** Nickname del usuario con sesión iniciada en el hub, o null si nadie ha iniciado sesión. */
  public java.lang.String getNickname() throws android.os.RemoteException;
  /** Mejor score del usuario actual en ese juego (0 si no tiene). */
  public int getMejorScore(java.lang.String juego) throws android.os.RemoteException;
  /**
   * Guarda un score para el usuario actual. El hub agrega la ciudad (GPS) por su cuenta.
   * Devuelve false si no hay sesión o el juego no existe.
   */
  public boolean guardarScore(java.lang.String juego, int puntos) throws android.os.RemoteException;
}
