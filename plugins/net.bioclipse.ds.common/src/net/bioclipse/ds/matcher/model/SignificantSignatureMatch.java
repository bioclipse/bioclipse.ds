package net.bioclipse.ds.matcher.model;

import org.eclipse.ui.views.properties.IPropertySource;

import net.bioclipse.ds.model.result.PosNegIncMatch;

/**
 * 
 * @author ola
 */
public class SignificantSignatureMatch extends PosNegIncMatch{

	private SignificantSignature significantSignature;

	public SignificantSignatureMatch(SignificantSignature sign, int resultStatus) {
		super(sign.getSignature(), resultStatus);
		this.significantSignature=sign;
	}

	public SignificantSignature getSignificantSignature() {
		return significantSignature;
	}
	public void setSignificantSignature(SignificantSignature significantSignature) {
		this.significantSignature = significantSignature;
	}
	
	@Override
	public String getSuffix() {
//		if (significantSignature!=null)
//			return " [height=" + significantSignature.getHeight() + "]";
//		else
			return "";
	}

	@Override
	public String toString() {
		return significantSignature.toString();
	}

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getAdapter( Class adapter ) {

        if (adapter.isAssignableFrom(IPropertySource.class)) {
            return new SignificantSignatureMatchPropertySource(this);
        }
        return super.getAdapter( adapter );
    }

}
